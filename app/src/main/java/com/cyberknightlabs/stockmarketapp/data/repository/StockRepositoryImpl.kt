package com.cyberknightlabs.stockmarketapp.data.repository

import com.cyberknightlabs.stockmarketapp.data.csv.CSVParser
import com.cyberknightlabs.stockmarketapp.data.csv.IntradayInfoParser
import com.cyberknightlabs.stockmarketapp.data.local.StockDatabase
import com.cyberknightlabs.stockmarketapp.data.mapper.toCompanyInfo
import com.cyberknightlabs.stockmarketapp.data.mapper.toCompanyListing
import com.cyberknightlabs.stockmarketapp.data.mapper.toCompanyListingEntity
import com.cyberknightlabs.stockmarketapp.data.remote.dto.StockApi
import com.cyberknightlabs.stockmarketapp.domain.model.CompanyInfo
import com.cyberknightlabs.stockmarketapp.domain.model.CompanyListing
import com.cyberknightlabs.stockmarketapp.domain.model.IntradayInfo
import com.cyberknightlabs.stockmarketapp.domain.repository.StockRepository
import com.cyberknightlabs.stockmarketapp.util.Resource
import com.squareup.moshi.subtypeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    val api: StockApi,
    val db:StockDatabase,
    val companyListingParser: CSVParser<CompanyListing>,
    val intradayInfoParser: CSVParser<IntradayInfo>
):StockRepository {
    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow{
            emit(Resource.Loading(true))

            val localListings = dao.searchCompanyListing(query)

            emit(Resource.Success(data = localListings.map { it.toCompanyListing() }))

            val isDbEmpty = localListings.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote

            if(shouldJustLoadFromCache){
                emit(Resource.Loading(false))
                return@flow
            }

            val remoteListings = try{
                val response = api.getListing()
                companyListingParser.parse(response.byteStream())
            }catch (e: IOException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }catch (e:HttpException){
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }

            remoteListings?.let { listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListings(
                    listings.map {it.toCompanyListingEntity()}
                )
                emit(Resource.Success(data = dao.searchCompanyListing("").map { it.toCompanyListing() }))
                emit(Resource.Loading(false))
            }
        }
    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try{
            val response = api.getIntradayInfo(symbol)
            val results = intradayInfoParser.parse(response.byteStream())
            Resource.Success(results)
        }catch (e:IOException){
            e.printStackTrace()
            Resource.Error(message = "Couldn't load intraday info")
        }catch (e:HttpException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load intraday info")
        }
    }

    override suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> {
        return try{
            val result = api.getCompanyInfo(symbol)
            Resource.Success(result.toCompanyInfo())
        }catch (e:IOException){
            e.printStackTrace()
            Resource.Error(message = "Couldn't load company info")
        }catch (e:HttpException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load company info")
        }
    }
}