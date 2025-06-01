package com.cyberknightlabs.stockmarketapp.presentation.company_listings

import com.cyberknightlabs.stockmarketapp.domain.model.CompanyListing

data class CompanyListingStates (
    val companies:List<CompanyListing> = emptyList(),
    val isLoading:Boolean = false,
    val isRefreshing:Boolean = false,
    val searchQuery:String = ""
)