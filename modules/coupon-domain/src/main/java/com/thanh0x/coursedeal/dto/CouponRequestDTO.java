package com.thanh0x.coursedeal.dto;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Coupon URL is required")
    private String couponUrl;
}



