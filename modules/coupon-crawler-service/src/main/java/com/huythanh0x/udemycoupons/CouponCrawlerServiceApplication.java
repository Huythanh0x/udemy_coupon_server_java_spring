package com.huythanh0x.udemycoupons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.huythanh0x.udemycoupons")
public class CouponCrawlerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponCrawlerServiceApplication.class, args);
    }
}

