package com.codecatalysts.payright.repositories;

import com.codecatalysts.payright.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByRelatedSubscriptionIdAndTypeOrderByCreatedAtDesc(String relatedSubscriptionId, String type);
}
