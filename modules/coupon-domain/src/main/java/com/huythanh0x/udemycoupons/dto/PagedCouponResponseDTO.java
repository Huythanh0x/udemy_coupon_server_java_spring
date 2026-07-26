package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data class representing a response containing a paginated list of coupon data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedCouponResponseDTO {
    Long lastFetchTime; // Epoch milliseconds
    Long totalCoupon;
    Integer totalPage;
    Integer currentPage;
    List<CouponSummaryDTO> courses;
}
