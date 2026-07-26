package com.thanh0x.coursedeal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous task execution in the application.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${custom.async.scraper.core-pool-size:5}")
    private int corePoolSize;

    @Value("${custom.async.scraper.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${custom.async.scraper.queue-capacity:100}")
    private int queueCapacity;

    /**
     * Dedicated thread pool for Udemy scraping tasks.
     * 
     * @return Executor instance for background scraping
     */
    @Bean(name = "scraperExecutor")
    public Executor scraperExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("UdemyScraper-");
        executor.initialize();
        return executor;
    }
}
