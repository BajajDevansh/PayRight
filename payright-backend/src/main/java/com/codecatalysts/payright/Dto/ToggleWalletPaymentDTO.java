package com.codecatalysts.payright.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ToggleWalletPaymentDTO {
    @NotNull
    private Boolean enable;
}