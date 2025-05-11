package com.codecatalysts.payright.service;

import com.codecatalysts.payright.model.Notification;
import com.codecatalysts.payright.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(String userId, String message, String type, String relatedSubscriptionId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        if (relatedSubscriptionId != null) {
            notification.setRelatedSubscriptionId(relatedSubscriptionId);
        }
        Notification savedNotification = notificationRepository.save(notification);
        logger.info("Created notification for user {}: type={}, message='{}'", userId, type, message.substring(0, Math.min(message.length(), 50)));
        return savedNotification;
    }

    public List<Notification> getUnreadNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getAllNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(String userId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
    public List<Notification> getNotificationsForSubscriptionByType(String subscriptionId, String type) {
        return notificationRepository.findByRelatedSubscriptionIdAndTypeOrderByCreatedAtDesc(subscriptionId, type);
    }
}
