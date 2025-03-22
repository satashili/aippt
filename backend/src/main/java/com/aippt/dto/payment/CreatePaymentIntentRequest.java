package com.aippt.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentRequest {
    
    /**
     * Plan type, e.g.: monthly, annual
     */
    private String planType;
    
    /**
     * Amount (in cents)
     */
    private Long amount;
    
    /**
     * Currency, default is USD
     */
    @Builder.Default
    private String currency = "usd";
} 