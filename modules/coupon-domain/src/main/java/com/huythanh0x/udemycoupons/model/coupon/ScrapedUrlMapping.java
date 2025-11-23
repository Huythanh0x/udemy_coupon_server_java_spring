package com.huythanh0x.udemycoupons.model.coupon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for mapping scraped URLs to their corresponding Udemy coupon URLs.
 * This allows us to track the source of each coupon and avoid re-processing
 * the same scraped URLs multiple times.
 */
@Entity
@Table(name = "scraped_url_mapping", indexes = {
    @Index(name = "idx_coupon_url", columnList = "coupon_url"),
    @Index(name = "idx_crawler_source", columnList = "crawler_source")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScrapedUrlMapping {
    @Id
    @Column(name = "scraped_url", length = 500)
    private String scrapedUrl;
    
    @Column(name = "coupon_url", nullable = false, length = 255)
    private String couponUrl;
    
    @Column(name = "crawler_source", nullable = false, length = 50)
    private String crawlerSource;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

