package com.codecatalysts.payright.Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AISubscriptionAnalysisRequest {
    private List<TransactionAIAttributes> transactions;

    public List<TransactionAIAttributes> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionAIAttributes> transactions) {
        this.transactions = transactions;
    }

    @Data
    public static class TransactionAIAttributes {
        public TransactionAIAttributes(){

        }

        public TransactionAIAttributes(String id, String description, BigDecimal amount, LocalDate date, String currency) {
            this.id = id;
            this.description = description;
            this.amount = amount;
            this.date = date;
            this.currency = currency;
        }

        private String id;
        private String description;
        private BigDecimal amount;
        private LocalDate date;
        private String currency;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
}

