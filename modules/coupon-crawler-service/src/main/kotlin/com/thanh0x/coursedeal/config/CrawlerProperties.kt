package com.thanh0x.coursedeal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "custom")
data class CrawlerProperties(
    var intervalTime: Long = 900000,
    var numberOfRequestThread: Int = 4,
    var numberOfEnextCoupon: Int = 500,
    var numberOfRealDiscountCoupon: Int = 500,
    var skipRecentlyCheckedExpiredHours: Int = 12,
    var refreshExpiringHours: Int = 2,
    var refreshMinUsesRemaining: Int = 50,
    var refreshOldHours: Int = 1,
    var enableSmartRefresh: Boolean = true,
    var batchProcessingSize: Int = 100,
)
