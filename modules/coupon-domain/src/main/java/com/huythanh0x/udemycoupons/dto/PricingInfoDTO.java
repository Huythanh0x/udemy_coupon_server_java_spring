package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for course pricing information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingInfoDTO {
    private Float price;
    private Float listPrice;
    private Float savingPrice;
    private String currency;
    private String priceString; // e.g., "Free"
    private String currencySymbol;
    private Integer discountPercent;
    private String discountDeadlineText; // e.g., "4 days"
    private String couponCode;
    private Integer usesRemaining;
    private Integer maximumUses;
}

