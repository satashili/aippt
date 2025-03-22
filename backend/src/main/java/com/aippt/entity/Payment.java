package com.aippt.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Payment record entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("payments")
public class Payment {
    
    /**
     * Payment ID
     */
    @TableId
    private String id;
    
    /**
     * User ID
     */
    private String userId;
    
    /**
     * Stripe payment intent ID
     */
    private String paymentIntentId;
    
    /**
     * Plan type (monthly/annual)
     */
    private String planType;
    
    /**
     * Payment amount (in cents)
     */
    private Long amount;
    
    /**
     * Currency
     */
    private String currency;
    
    /**
     * Payment status
     * PENDING - Payment pending
     * SUCCEEDED - Payment successful
     * FAILED - Payment failed
     * REFUNDED - Payment refunded
     */
    private String status;
    
    /**
     * Creation time
     */
    private LocalDateTime createdAt;
    
    /**
     * Update time
     */
    private LocalDateTime updatedAt;
    
    /**
     * Subscription start time
     */
    private LocalDateTime startDate;
    
    /**
     * Subscription end time
     */
    private LocalDateTime endDate;
    
    /**
     * Cancellation flag
     */
    private Boolean cancelled;
    
    /**
     * Cancellation time
     */
    private LocalDateTime cancelledAt;
    
    /**
     * Additional metadata (JSON format)
     */
    private String metadata;
} 