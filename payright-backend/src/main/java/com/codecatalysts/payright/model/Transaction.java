package com.codecatalysts.payright.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getRecurringCandidate() {
        return isRecurringCandidate;
    }

    public void setRecurringCandidate(Boolean recurringCandidate) {
        isRecurringCandidate = recurringCandidate;
    }

    public String getPotentialSubscriptionId() {
        return potentialSubscriptionId;
    }

    public void setPotentialSubscriptionId(String potentialSubscriptionId) {
        this.potentialSubscriptionId = potentialSubscriptionId;
    }

    private LocalDate date;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String source;
    private Boolean isRecurringCandidate;
    private String potentialSubscriptionId;
    private boolean coveredByWalletPayment = false;
}

