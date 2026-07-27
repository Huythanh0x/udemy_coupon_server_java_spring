package com.thanh0x.coursedeal.repository.audit;

import com.thanh0x.coursedeal.model.audit.ScrapingTaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapingTaskLogRepository extends JpaRepository<ScrapingTaskLog, Long> {
}
