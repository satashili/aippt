package com.aippt.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailsResponse {
    
    /**
     * Order ID
     */
    private String orderId;
    
    /**
     * Payment intent ID
     */
    private String paymentIntentId;
    
    /**
     * Plan type
     */
    private String planType;
    
    /**
     * Subscription status
     */
    private String status;
    
    /**
     * Amount (in cents)
     */
    private Long amount;
    
    /**
     * Currency
     */
    private String currency;
    
    /**
     * Creation time
     */
    private LocalDateTime createdAt;
    
    /**
     * Subscription end date
     */
    private LocalDateTime endDate;
} 