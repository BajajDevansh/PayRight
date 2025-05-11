package com.codecatalysts.payright.repositories;

import com.codecatalysts.payright.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    List<Subscription> findByUserIdAndActiveTrueOrderByNextBillingDateAsc(String userId);
    List<Subscription> findByUserIdOrderByStartDateDesc(String userId);
    List<Subscription> findByNextBillingDateBeforeAndActiveTrueAndAutoRenewsTrue(LocalDate date);
    List<Subscription> findByNextBillingDateBeforeAndActiveTrueAndAutoRenewsTrueAndPayFromWalletTrue(LocalDate date);
}
