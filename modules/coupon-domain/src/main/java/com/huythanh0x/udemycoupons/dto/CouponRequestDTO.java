package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for creating a new coupon.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponRequestDTO {
    /**
     * The Udemy coupon URL.
     */
    private String couponUrl;
}



