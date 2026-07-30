package com.thanh0x.coursedeal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.thanh0x.coursedeal"])
class CouponCrawlerServiceApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    // runApplication's vararg signature requires spreading the incoming args array.
    runApplication<CouponCrawlerServiceApplication>(*args)
}
