package com.thanh0x.coursedeal.dto

/**
 * DTO for course pricing information.
 */
data class PricingInfoDTO(
    val price: Float? = null,
    val listPrice: Float? = null,
    val savingPrice: Float? = null,
    val currency: String? = null,
    // e.g., "Free"
    val priceString: String? = null,
    val currencySymbol: String? = null,
    val discountPercent: Int? = null,
    // e.g., "4 days"
    val discountDeadlineText: String? = null,
    val couponCode: String? = null,
    val usesRemaining: Int? = null,
    val maximumUses: Int? = null,
)
