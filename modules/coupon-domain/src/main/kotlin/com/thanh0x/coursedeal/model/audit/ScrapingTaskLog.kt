package com.thanh0x.coursedeal.model.audit

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entity for auditing asynchronous scraping tasks.
 */
@Entity
@Table(name = "scraping_task_logs")
class ScrapingTaskLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var url: String? = null,

    var status: String? = null, // SUCCESS, FAILED, PENDING

    @Column(columnDefinition = "TEXT")
    var errorMessage: String? = null,

    var remoteAddr: String? = null,

    var courseId: Int? = null,

    @CreationTimestamp
    var createdAt: LocalDateTime? = null
)
