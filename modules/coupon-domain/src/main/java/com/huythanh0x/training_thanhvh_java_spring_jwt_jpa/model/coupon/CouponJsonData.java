package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CouponJsonData {
    private Float price;
    private String expiredDate;
    private String previewImage;
    private String previewVideo;
    private int usesRemaining;
}
