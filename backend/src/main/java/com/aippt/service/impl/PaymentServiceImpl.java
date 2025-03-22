package com.aippt.service.impl;

import com.aippt.dto.payment.CreatePaymentIntentRequest;
import com.aippt.dto.payment.CreatePaymentIntentResponse;
import com.aippt.dto.payment.PaymentDetailsResponse;
import com.aippt.entity.Payment;
import com.aippt.entity.User;
import com.aippt.exception.PaymentProcessingException;
import com.aippt.exception.UserNotFoundException;
import com.aippt.mapper.PaymentMapper;
import com.aippt.mapper.UserMapper;
import com.aippt.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.apiKey}")
    private String stripeApiKey;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    @Autowired
    private PaymentMapper paymentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * Create payment intent
     */
    @Override
    @Transactional
    public CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        log.info("Creating payment intent: {}", request);
        
        try {
            // Initialize Stripe
            Stripe.apiKey = stripeApiKey;
            
            // Create metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("planType", request.getPlanType());
            
            // Build payment intent parameters
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .putAllMetadata(metadata)
                .build();
            
            // Create payment intent
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            // Save payment record
            Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .paymentIntentId(paymentIntent.getId())
                .planType(request.getPlanType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .cancelled(false)
                .build();
            
            paymentMapper.insert(payment);
            
            // Build response
            return CreatePaymentIntentResponse.builder()
                .clientSecret(paymentIntent.getClientSecret())
                .paymentIntentId(paymentIntent.getId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();
            
        } catch (StripeException e) {
            log.error("Error creating payment intent", e);
            return CreatePaymentIntentResponse.builder()
                .error("Payment processing failed: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Process payment success callback
     */
    @Override
    @Transactional
    public boolean processPaymentSuccess(String paymentIntentId, String userId) {
        log.info("Processing payment success callback: paymentIntentId={}, userId={}", paymentIntentId, userId);
        
        try {
            // 查询支付记录
            LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Payment::getPaymentIntentId, paymentIntentId);
            Payment payment = paymentMapper.selectOne(queryWrapper);
            
            if (payment == null) {
                log.error("Payment record not found: {}", paymentIntentId);
                return false;
            }
            
            // 检查是否已处理，实现幂等性
            if ("SUCCEEDED".equals(payment.getStatus()) && userId.equals(payment.getUserId())) {
                log.info("支付已处理，避免重复操作: {}", paymentIntentId);
                return true;
            }
            
            // 继续原有逻辑...
            
            // Update payment status
            payment.setUserId(userId);
            payment.setStatus("SUCCEEDED");
            payment.setUpdatedAt(LocalDateTime.now());
            
            // Set subscription time
            LocalDateTime now = LocalDateTime.now();
            payment.setStartDate(now);
            
            // Set end time based on plan type
            if ("monthly".equals(payment.getPlanType())) {
                payment.setEndDate(now.plusMonths(1));
            } else if ("annual".equals(payment.getPlanType())) {
                payment.setEndDate(now.plusYears(1));
            } else {
                // Default 30 days
                payment.setEndDate(now.plusDays(30));
            }
            
            paymentMapper.updateById(payment);
            
            // Update user membership status
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.error("User not found: {}", userId);
                throw new UserNotFoundException("User not found: " + userId);
            }
            
            user.setIsMember(true);
            user.setMemberExpireTime(payment.getEndDate());
            userMapper.updateById(user);
            
            log.info("Payment processing successful: {}", paymentIntentId);
            return true;
            
        } catch (Exception e) {
            log.error("Error processing payment callback", e);
            throw new PaymentProcessingException("Error processing payment callback: " + e.getMessage());
        }
    }
    
    /**
     * Get payment details
     */
    @Override
    public PaymentDetailsResponse getPaymentDetails(String userId) {
        log.info("Getting user payment details: {}", userId);
        
        // Query user's most recent successful payment record
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Payment::getUserId, userId)
                   .eq(Payment::getStatus, "SUCCEEDED")
                   .orderByDesc(Payment::getCreatedAt)
                   .last("LIMIT 1");
        
        Payment payment = paymentMapper.selectOne(queryWrapper);
        
        if (payment == null) {
            log.warn("No payment record found for user: {}", userId);
            return PaymentDetailsResponse.builder().build();
        }
        
        return PaymentDetailsResponse.builder()
            .orderId(payment.getId())
            .paymentIntentId(payment.getPaymentIntentId())
            .planType(payment.getPlanType())
            .status(payment.getStatus())
            .amount(payment.getAmount())
            .currency(payment.getCurrency())
            .createdAt(payment.getCreatedAt())
            .endDate(payment.getEndDate())
            .build();
    }
    
    /**
     * Cancel subscription
     */
    @Override
    @Transactional
    public boolean cancelSubscription(String userId) {
        log.info("Cancelling user subscription: {}", userId);
        
        // Query user's most recent active subscription
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Payment::getUserId, userId)
                   .eq(Payment::getStatus, "SUCCEEDED")
                   .eq(Payment::getCancelled, false)
                   .orderByDesc(Payment::getCreatedAt)
                   .last("LIMIT 1");
        
        Payment payment = paymentMapper.selectOne(queryWrapper);
        
        if (payment == null) {
            log.warn("No active subscription found for user: {}", userId);
            return false;
        }
        
        // Update payment record
        payment.setCancelled(true);
        payment.setCancelledAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(payment);
        
        // If immediately cancelling membership status, update user status
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            throw new UserNotFoundException("User not found: " + userId);
        }
        
        // Option to immediately cancel membership or let it expire naturally
        // Here we choose to let membership expire naturally
        // user.setIsMember(false);
        // user.setMemberExpireTime(null);
        // userMapper.updateById(user);
        
        log.info("User subscription cancelled: {}", userId);
        return true;
    }

    @Override
    public Payment findByPaymentIntentId(String paymentIntentId) {
        LambdaQueryWrapper<Payment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Payment::getPaymentIntentId, paymentIntentId);
        return paymentMapper.selectOne(queryWrapper);
    }
} 