package com.thanh0x.coursedeal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.thanh0x.coursedeal")
public class CouponCrawlerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponCrawlerServiceApplication.class, args);
    }
}

