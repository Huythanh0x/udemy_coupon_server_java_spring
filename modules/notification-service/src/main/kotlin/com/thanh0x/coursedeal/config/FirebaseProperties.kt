package com.thanh0x.coursedeal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "custom.firebase")
data class FirebaseProperties(
    var configPath: String = "",
)
