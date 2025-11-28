package com.huythanh0x.udemycoupons.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for updating an existing coupon.
 * <p>
 * For now it is intentionally minimal; extend with more updatable fields when needed.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponUpdateRequestDTO {
    /**
     * The identifier of the coupon to update.
     */
    private Integer courseId;
}



