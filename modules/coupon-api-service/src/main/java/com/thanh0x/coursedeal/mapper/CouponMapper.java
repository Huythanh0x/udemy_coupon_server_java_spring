package com.thanh0x.coursedeal.mapper;

import com.thanh0x.coursedeal.dto.CouponDetailDTO;
import com.thanh0x.coursedeal.dto.CouponSummaryDTO;
import com.thanh0x.coursedeal.model.coupon.CouponCourseData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    @Mapping(target = "isNew", source = "new")
    CouponSummaryDTO toSummaryDto(CouponCourseData entity);

    @Mapping(target = "isNew", source = "new")
    CouponDetailDTO toDetailDto(CouponCourseData entity);
}
