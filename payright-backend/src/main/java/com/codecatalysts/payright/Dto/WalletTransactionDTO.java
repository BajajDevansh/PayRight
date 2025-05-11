package com.codecatalysts.payright.Dto;

import com.codecatalysts.payright.model.WalletTransaction;
import com.codecatalysts.payright.model.WalletTransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletTransactionDTO {
    private String id;
    private WalletTransactionType type;
    private BigDecimal amount;
    private String currency;
    private String description;
    private LocalDateTime transactionDate;
    private String relatedSubscriptionId;

    public static WalletTransactionDTO fromEntity(WalletTransaction entity) {
        if (entity == null) return null;
        WalletTransactionDTO dto = new WalletTransactionDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setDescription(entity.getDescription());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setRelatedSubscriptionId(entity.getRelatedSubscriptionId());
        return dto;
    }
}
