package com.thanh0x.coursedeal.dto

/**
 * DTO for course pricing information.
 */
data class PricingInfoDTO(
    val price: Float? = null,
    val listPrice: Float? = null,
    val savingPrice: Float? = null,
    val currency: String? = null,
    val priceString: String? = null, // e.g., "Free"
    val currencySymbol: String? = null,
    val discountPercent: Int? = null,
    val discountDeadlineText: String? = null, // e.g., "4 days"
    val couponCode: String? = null,
    val usesRemaining: Int? = null,
    val maximumUses: Int? = null,
)
