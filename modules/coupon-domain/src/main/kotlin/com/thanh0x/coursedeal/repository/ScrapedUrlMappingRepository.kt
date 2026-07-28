package com.thanh0x.coursedeal.repository

import com.thanh0x.coursedeal.model.coupon.ScrapedUrlMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ScrapedUrlMappingRepository : JpaRepository<ScrapedUrlMapping, String> {
    
    fun findByScrapedUrl(scrapedUrl: String): ScrapedUrlMapping?
    
    fun findByCouponUrl(couponUrl: String): List<ScrapedUrlMapping>
    
    fun findByCrawlerSource(crawlerSource: String): List<ScrapedUrlMapping>
    
    @Query("SELECT s.couponUrl FROM ScrapedUrlMapping s WHERE s.couponUrl IS NOT NULL")
    fun findAllCouponUrls(): Set<String>
}
