package com.thanh0x.coursedeal.model.audit

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entity for auditing asynchronous scraping tasks.
 */
@Entity
@Table(name = "scraping_task_logs")
@Suppress("LongParameterList")
class ScrapingTaskLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var url: String? = null,
    // SUCCESS, FAILED, PENDING
    var status: String? = null,
    @Column(columnDefinition = "TEXT")
    var errorMessage: String? = null,
    var remoteAddr: String? = null,
    var courseId: Int? = null,
    @CreationTimestamp
    var createdAt: LocalDateTime? = null,
)
