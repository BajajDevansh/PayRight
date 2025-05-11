package com.codecatalysts.payright.Dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AlternativeSuggestionDTO {
    private String name;
    private String originalAppName;
    private String description;
    private String type;
    private BigDecimal price;
    private String priceFrequency;
    private String link;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalAppName() {
        return originalAppName;
    }

    public void setOriginalAppName(String originalAppName) {
        this.originalAppName = originalAppName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPriceFrequency() {
        return priceFrequency;
    }

    public void setPriceFrequency(String priceFrequency) {
        this.priceFrequency = priceFrequency;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    private double relevanceScore;
}
