package com.thanh0x.coursedeal.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.model.coupon.CouponCourseData
import com.thanh0x.coursedeal.model.user.UserEntity
import com.thanh0x.coursedeal.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class NotificationService(private val userRepository: UserRepository) {
    private val log = logger()

    /**
     * Sends personalized notifications to users interested in the new coupon.
     */
    fun notifyInterestedUsers(coupon: CouponCourseData) {
        val allUsers = userRepository.findAll()

        for (user in allUsers) {
            val token = user.fcmToken
            if (token.isNullOrEmpty()) {
                continue
            }

            if (isUserInterested(user, coupon)) {
                sendPush(
                    token,
                    "Course Deal: " + coupon.category,
                    "Free: " + coupon.title,
                )
            }
        }
    }

    private fun isUserInterested(
        user: UserEntity,
        coupon: CouponCourseData,
    ): Boolean {
        val pref = user.preference ?: return false

        val categoryMatch =
            pref.notificationsEnabled &&
                pref.categories.any { cat ->
                    cat.equals(coupon.category, ignoreCase = true)
                }

        val title = coupon.title?.lowercase() ?: ""
        val keywordMatch =
            pref.notificationsEnabled &&
                pref.keywords.any { keyword ->
                    title.contains(keyword.lowercase())
                }

        return categoryMatch || keywordMatch
    }

    fun sendPush(
        token: String,
        title: String,
        body: String,
    ) {
        try {
            val message =
                Message.builder()
                    .setNotification(
                        Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build(),
                    )
                    .setToken(token)
                    .build()

            val response = FirebaseMessaging.getInstance().send(message)
            log.info("Successfully sent message: $response")
        } catch (e: Exception) {
            log.error("Error sending FCM message", e)
        }
    }
}
