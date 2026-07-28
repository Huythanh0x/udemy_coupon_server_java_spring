package com.thanh0x.coursedeal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.thanh0x.coursedeal"])
class CouponApiServiceApplication

fun main(args: Array<String>) {
    runApplication<CouponApiServiceApplication>(*args)
}
