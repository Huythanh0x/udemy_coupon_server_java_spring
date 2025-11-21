package com.huythanh0x.udemycoupons.model.coupon;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpiredCourseData {
    @Id
    String couponUrl;
    @CreationTimestamp
    Timestamp timeStamp;

    public ExpiredCourseData(String couponUrl) {
        this.couponUrl = couponUrl;
    }
}
