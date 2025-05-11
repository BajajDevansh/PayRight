package com.codecatalysts.payright.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancellationLinkDTO {
    private String subscriptionId;
    private String subscriptionName;
    private String url;
    private String message;
}
