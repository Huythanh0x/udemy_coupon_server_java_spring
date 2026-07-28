package com.thanh0x.coursedeal.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.io.IOException

@Configuration
class FirebaseConfig {

    @Value("\${custom.firebase.config-path:}")
    private lateinit var configPath: String

    @PostConstruct
    fun initialize() {
        if (configPath.isEmpty()) {
            log.warn("Firebase config path is not set. FCM will be disabled.")
            return
        }

        try {
            val serviceAccount = FileInputStream(configPath)
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                log.info("Firebase Admin SDK initialized successfully.")
            }
        } catch (e: IOException) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.message)
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(FirebaseConfig::class.java)
    }
}
