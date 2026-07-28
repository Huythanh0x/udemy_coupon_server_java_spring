package com.thanh0x.coursedeal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "custom.firebase")
data class FirebaseProperties(
    var configPath: String = "",
)

@ConfigurationProperties(prefix = "custom.async.scraper")
data class AsyncScraperProperties(
    var corePoolSize: Int = 5,
    var maxPoolSize: Int = 10,
    var queueCapacity: Int = 100,
)
