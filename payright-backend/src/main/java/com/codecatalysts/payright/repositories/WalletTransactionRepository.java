package com.codecatalysts.payright.repositories;

import com.codecatalysts.payright.model.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WalletTransactionRepository extends MongoRepository<WalletTransaction, String> {
    List<WalletTransaction> findByUserIdOrderByTransactionDateDesc(String userId);
    Page<WalletTransaction> findByUserIdOrderByTransactionDateDesc(String userId, Pageable pageable);
    List<WalletTransaction> findByUserIdAndRelatedSubscriptionIdAndTypeOrderByTransactionDateDesc(
            String userId, String relatedSubscriptionId, com.codecatalysts.payright.model.WalletTransactionType type
    );
}
