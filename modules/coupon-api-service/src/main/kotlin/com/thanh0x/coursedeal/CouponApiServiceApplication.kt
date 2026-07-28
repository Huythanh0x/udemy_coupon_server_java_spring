package com.thanh0x.coursedeal

import com.thanh0x.coursedeal.config.ApiProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ApiProperties::class)
class CouponApiServiceApplication

fun main(args: Array<String>) {
    runApplication<CouponApiServiceApplication>(*args)
}
