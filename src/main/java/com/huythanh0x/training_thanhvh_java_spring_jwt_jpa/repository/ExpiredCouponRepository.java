package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.repository;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.model.coupon.ExpiredCourseData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpiredCouponRepository extends JpaRepository<ExpiredCourseData, Integer> {
}
