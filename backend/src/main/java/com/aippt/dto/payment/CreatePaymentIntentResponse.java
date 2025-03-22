package com.aippt.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentResponse {
    
    /**
     * Client secret, used for initializing Stripe Elements in frontend
     */
    private String clientSecret;
    
    /**
     * Payment intent ID
     */
    private String paymentIntentId;
    
    /**
     * Amount (in cents)
     */
    private Long amount;
    
    /**
     * Currency
     */
    private String currency;
    
    /**
     * Error message (if any)
     */
    private String error;
} 