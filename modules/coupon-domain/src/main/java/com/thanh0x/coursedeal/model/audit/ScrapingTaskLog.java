package com.thanh0x.coursedeal.model.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for auditing asynchronous scraping tasks.
 */
@Entity
@Table(name = "scraping_task_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapingTaskLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    private String status; // SUCCESS, FAILED, PENDING

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String remoteAddr;

    private Integer courseId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
