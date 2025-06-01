package com.cyberknightlabs.stockmarketapp.di

import com.cyberknightlabs.stockmarketapp.data.csv.CSVParser
import com.cyberknightlabs.stockmarketapp.data.csv.CompanyListingParser
import com.cyberknightlabs.stockmarketapp.data.csv.IntradayInfoParser
import com.cyberknightlabs.stockmarketapp.data.repository.StockRepositoryImpl
import com.cyberknightlabs.stockmarketapp.domain.model.CompanyListing
import com.cyberknightlabs.stockmarketapp.domain.model.IntradayInfo
import com.cyberknightlabs.stockmarketapp.domain.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCompanyListingParser(
        companyListingParser: CompanyListingParser
    ):CSVParser<CompanyListing>

    @Binds
    @Singleton
    abstract fun bindIntradayInfoParser(
        intradayInfoParser: IntradayInfoParser
    ):CSVParser<IntradayInfo>


    @Binds
    @Singleton
    abstract fun bindStockRepository(
        stockRepositoryImpl: StockRepositoryImpl
    ):StockRepository

}