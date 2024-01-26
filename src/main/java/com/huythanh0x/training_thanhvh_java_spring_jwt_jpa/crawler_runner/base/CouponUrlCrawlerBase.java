package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.base;

import java.util.List;

public abstract class CouponUrlCrawlerBase {
    String apiUrl;

    public abstract List<String> getAllCouponUrls();
}
