package com.thanh0x.coursedeal.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.thanh0x.coursedeal.model.coupon.CouponCourseData;
import com.thanh0x.coursedeal.model.user.UserEntity;
import com.thanh0x.coursedeal.model.user.UserPreference;
import com.thanh0x.coursedeal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final UserRepository userRepository;

    public NotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Sends personalized notifications to users interested in the new coupon.
     */
    public void notifyInterestedUsers(CouponCourseData coupon) {
        List<UserEntity> allUsers = userRepository.findAll();
        
        for (UserEntity user : allUsers) {
            if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
                continue;
            }

            if (isUserInterested(user, coupon)) {
                sendPush(user.getFcmToken(), 
                        "Course Deal: " + coupon.getCategory(), 
                        "Free: " + coupon.getTitle());
            }
        }
    }

    private boolean isUserInterested(UserEntity user, CouponCourseData coupon) {
        UserPreference pref = user.getPreference();
        
        // If no preference set, default to no notifications or broadcast?
        // Let's default to no notifications if they haven't set up preferences yet
        if (pref == null || !pref.isNotificationsEnabled()) {
            return false;
        }

        // Match category (case insensitive)
        boolean categoryMatch = pref.getCategories().stream()
                .anyMatch(cat -> cat.equalsIgnoreCase(coupon.getCategory()));
        
        if (categoryMatch) return true;

        // Match keywords in title (case insensitive)
        String title = coupon.getTitle().toLowerCase();
        return pref.getKeywords().stream()
                .anyMatch(keyword -> title.contains(keyword.toLowerCase()));
    }

    public void sendPush(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setToken(token)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message: " + response);
        } catch (Exception e) {
            log.error("Error sending FCM message", e);
        }
    }
}
