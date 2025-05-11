package com.codecatalysts.payright.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Document(collection = "subscriptions")
public class Subscription {
    @Id
    private String id;
    private String userId;
    private String name;
    private String detectedDescription;
    private BigDecimal amount;
    private String currency;
    private String frequency;
    private LocalDate nextBillingDate;
    private LocalDate startDate;
    private boolean active = true;
    private String category;
    private String notes;
    private boolean autoRenews = true;
    private double usageScore;
    private String cancellationUrl;
    private LocalDate cancellationDate;
    private boolean payFromWallet = false;
}


