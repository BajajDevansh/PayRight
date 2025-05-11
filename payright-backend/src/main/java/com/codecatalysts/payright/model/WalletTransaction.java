package com.codecatalysts.payright.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "wallet_transactions")
public class WalletTransaction {
    @Id
    private String id;
    private String walletId; // Link to the Wallet
    private String userId;   // Denormalized for easier querying by user
    private WalletTransactionType type; // CREDIT (add funds), DEBIT (payment)
    private BigDecimal amount;
    private String currency;
    private String description; // e.g., "Added Funds", "Payment for Netflix Subscription"
    private LocalDateTime transactionDate;
    private String relatedSubscriptionId;
    private String relatedBankTransactionId;

    public WalletTransaction(String walletId, String userId, WalletTransactionType type, BigDecimal amount, String currency, String description) {
        this.walletId = walletId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.transactionDate = LocalDateTime.now();
    }
}
