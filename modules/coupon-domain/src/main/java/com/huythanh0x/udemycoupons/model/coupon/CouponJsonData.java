package com.huythanh0x.udemycoupons.model.coupon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponJsonData {
    private Float price;
    private Instant expiredDate;
    private String previewImage;
    private String previewVideo;
    private int usesRemaining;
}
