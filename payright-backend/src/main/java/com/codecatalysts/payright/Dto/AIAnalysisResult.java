package com.codecatalysts.payright.Dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AIAnalysisResult {
    private List<String> processedTransactionIds;
    private List<IdentifiedSubscriptionAI> identifiedSubscriptions;
    private List<String> alerts;

    public List<String> getProcessedTransactionIds() {
        return processedTransactionIds;
    }

    public void setProcessedTransactionIds(List<String> processedTransactionIds) {
        this.processedTransactionIds = processedTransactionIds;
    }

    public List<IdentifiedSubscriptionAI> getIdentifiedSubscriptions() {
        return identifiedSubscriptions;
    }

    public void setIdentifiedSubscriptions(List<IdentifiedSubscriptionAI> identifiedSubscriptions) {
        this.identifiedSubscriptions = identifiedSubscriptions;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<String> alerts) {
        this.alerts = alerts;
    }

    @Data
    public static class IdentifiedSubscriptionAI {
        private String name;
        private List<String> matchedTransactionIds;
        private BigDecimal estimatedAmount;
        private String currency;
        private String estimatedFrequency;
        private double confidenceScore;
        private String categoryGuess;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getMatchedTransactionIds() {
            return matchedTransactionIds;
        }

        public void setMatchedTransactionIds(List<String> matchedTransactionIds) {
            this.matchedTransactionIds = matchedTransactionIds;
        }

        public BigDecimal getEstimatedAmount() {
            return estimatedAmount;
        }

        public void setEstimatedAmount(BigDecimal estimatedAmount) {
            this.estimatedAmount = estimatedAmount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getEstimatedFrequency() {
            return estimatedFrequency;
        }

        public void setEstimatedFrequency(String estimatedFrequency) {
            this.estimatedFrequency = estimatedFrequency;
        }

        public double getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
        }

        public String getCategoryGuess() {
            return categoryGuess;
        }

        public void setCategoryGuess(String categoryGuess) {
            this.categoryGuess = categoryGuess;
        }
    }
}

