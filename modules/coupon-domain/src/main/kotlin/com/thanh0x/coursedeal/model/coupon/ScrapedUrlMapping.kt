package com.thanh0x.coursedeal.model.coupon

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * Entity for mapping scraped URLs to their corresponding Udemy coupon URLs.
 * This allows us to track the source of each coupon and avoid re-processing
 * the same scraped URLs multiple times.
 */
@Entity
@Table(
    name = "scraped_url_mapping",
    indexes = [
        Index(name = "idx_coupon_url", columnList = "coupon_url"),
        Index(name = "idx_crawler_source", columnList = "crawler_source")
    ]
)
class ScrapedUrlMapping(
    @Id
    @Column(name = "scraped_url", length = 500)
    var scrapedUrl: String = "",
    
    @Column(name = "coupon_url", nullable = false, length = 255)
    var couponUrl: String = "",
    
    @Column(name = "crawler_source", nullable = false, length = 50)
    var crawlerSource: String = "",
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
