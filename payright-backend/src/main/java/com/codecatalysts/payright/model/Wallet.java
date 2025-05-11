package com.codecatalysts.payright.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Document(collection = "wallets")
public class Wallet {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // Link to the User

    private BigDecimal balance;
    private String currency; // e.g., "USD" for simplicity

    public Wallet(String userId, BigDecimal initialBalance, String currency) {
        this.userId = userId;
        this.balance = initialBalance;
        this.currency = currency;
    }
}
