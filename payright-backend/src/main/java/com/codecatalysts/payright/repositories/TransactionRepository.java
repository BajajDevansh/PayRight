package com.codecatalysts.payright.repositories;

import com.codecatalysts.payright.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUserIdOrderByDateDesc(String userId);
}
