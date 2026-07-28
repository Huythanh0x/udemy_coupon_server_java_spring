package com.thanh0x.coursedeal.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.io.IOException

@Configuration
@EnableConfigurationProperties(FirebaseProperties::class)
class FirebaseConfig(private val properties: FirebaseProperties) {
    private val log = logger()

    @PostConstruct
    fun initialize() {
        if (properties.configPath.isEmpty()) {
            log.warn("Firebase config path is not set. FCM will be disabled.")
            return
        }

        try {
            val serviceAccount = FileInputStream(properties.configPath)
            val options =
                FirebaseOptions.builder()
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
}
