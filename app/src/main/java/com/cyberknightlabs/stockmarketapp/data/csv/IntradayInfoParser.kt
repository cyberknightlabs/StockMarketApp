package com.cyberknightlabs.stockmarketapp.data.csv

import android.annotation.SuppressLint
import com.cyberknightlabs.stockmarketapp.data.mapper.toIntradayInfo
import com.cyberknightlabs.stockmarketapp.data.remote.dto.IntradayInfoDto
import com.cyberknightlabs.stockmarketapp.domain.model.IntradayInfo
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntradayInfoParser @Inject constructor() : CSVParser<IntradayInfo> {

    @SuppressLint("NewApi")
    override suspend fun parse(stream: InputStream): List<IntradayInfo> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader.readAll()
                .drop(1)
                .mapNotNull { line ->
                    val timestamp = line.getOrNull(0) ?: return@mapNotNull null
                    val close = line.getOrNull(4) ?: return@mapNotNull null
                    val dto = IntradayInfoDto(timestamp, close.toDouble())
                    dto.toIntradayInfo()
                }.filter {
                    it.date.dayOfMonth == LocalDate.now().minusDays(4).dayOfMonth
                }.sortedBy {
                    it.date.hour
                }.also {
                    csvReader.close()
                }
        }
    }

}