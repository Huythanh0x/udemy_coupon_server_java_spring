package com.thanh0x.coursedeal.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.thanh0x.coursedeal.model.user.UserEntity;
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

    public void broadcastNewCoupon(String title, String category) {
        List<UserEntity> users = userRepository.findAll();
        for (UserEntity user : users) {
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                sendPush(user.getFcmToken(), "New Deal in " + category, title);
            }
        }
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
