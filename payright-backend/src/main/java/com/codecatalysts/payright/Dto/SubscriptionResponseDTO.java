package com.codecatalysts.payright.Dto;

import com.codecatalysts.payright.model.Subscription;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionResponseDTO {
    private String id;
    private String name;
    private BigDecimal amount;
    private String currency;
    private String frequency;
    private LocalDate nextBillingDate;
    private LocalDate startDate;
    private boolean active;
    private String category;
    private String notes;
    private boolean autoRenews;
    private String cancellationUrl;
    private boolean payFromWallet;
    private LocalDate cancellationDate;

    public static SubscriptionResponseDTO fromEntity(Subscription entity) {
        if (entity == null) return null;
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setFrequency(entity.getFrequency());
        dto.setNextBillingDate(entity.getNextBillingDate());
        dto.setStartDate(entity.getStartDate());
        dto.setActive(entity.isActive());
        dto.setCategory(entity.getCategory());
        dto.setNotes(

                entity.getNotes());
        dto.setAutoRenews(entity.isAutoRenews());
        dto.setCancellationUrl(entity.getCancellationUrl());
        dto.setCancellationDate(entity.getCancellationDate());
        dto.setPayFromWallet(entity.isPayFromWallet());
        return dto;
    }
}

