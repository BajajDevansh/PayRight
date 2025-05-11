package com.codecatalysts.payright.Dto;

import com.codecatalysts.payright.model.Wallet;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletDTO {
    private String id;
    private String userId;
    private BigDecimal balance;
    private String currency;

    public static WalletDTO fromEntity(Wallet wallet) {
        if (wallet == null) return null;
        WalletDTO dto = new WalletDTO();
        dto.setId(wallet.getId());
        dto.setUserId(wallet.getUserId());
        dto.setBalance(wallet.getBalance());
        dto.setCurrency(wallet.getCurrency());
        return dto;
    }
}

