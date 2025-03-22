package com.aippt.controller;

import com.aippt.dto.payment.CreatePaymentIntentRequest;
import com.aippt.dto.payment.CreatePaymentIntentResponse;
import com.aippt.dto.payment.PaymentDetailsResponse;
import com.aippt.entity.Payment;
import com.aippt.exception.PaymentProcessingException;
import com.aippt.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${stripe.publishableKey}")
    private String stripePublishableKey;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    @Autowired
    private PaymentService paymentService;
    
    // 可以使用内存缓存或数据库存储已处理的事件ID
    private final Set<String> processedEvents = Collections.synchronizedSet(new HashSet<>());
    
    /**
     * Get Stripe configuration information
     * Returns the public key needed for the frontend to initialize Stripe
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("publishableKey", stripePublishableKey);
        return ResponseEntity.ok(config);
    }
    
    /**
     * Create payment intent
     * Receives payment information from the frontend and creates a Stripe payment intent
     */
    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody CreatePaymentIntentRequest request, HttpSession session) {
        log.info("Received create payment intent request: {}", request);
        
        try {
            CreatePaymentIntentResponse response = paymentService.createPaymentIntent(request);
            
            if (response.getError() != null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", response.getError());
                return ResponseEntity.badRequest().body(error);
            }
            
            // Store payment information in session for later processing
            session.setAttribute("paymentIntentId", response.getPaymentIntentId());
            session.setAttribute("planType", request.getPlanType());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating payment intent", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Payment processing failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Payment success webhook
     * Processes subscription records and related business logic when payment is successful
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload, 
                                          @RequestHeader("Stripe-Signature") String signature) {
        log.info("收到Stripe webhook回调");
        
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            
            // 确保幂等性处理 - 检查是否已处理过此事件
            String eventId = event.getId();
            if (hasProcessedEvent(eventId)) {
                return ResponseEntity.ok("Event already processed");
            }
            
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
                    handlePaymentIntentSucceeded(paymentIntent);
                    break;
                // 添加其他事件类型...
            }
            
            // 记录已处理的事件
            markEventAsProcessed(eventId);
            return ResponseEntity.ok("Webhook handled successfully");
        } catch (Exception e) {
            log.error("处理webhook回调出错", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Webhook Error: " + e.getMessage());
        }
    }
    
    private void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        // 通过元数据或查询获取用户ID
        String userId = getUserIdFromPaymentIntent(paymentIntent);
        if (userId != null) {
            paymentService.processPaymentSuccess(paymentIntent.getId(), userId);
        } else {
            // 记录无法关联用户的支付
            log.warn("无法关联用户ID的支付成功: {}", paymentIntent.getId());
        }
    }
    
    /**
     * Get payment details
     * Used to display order information after successful payment
     */
    @GetMapping("/details")
    public ResponseEntity<?> getPaymentDetails(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No session information found");
            return ResponseEntity.badRequest().body(error);
        }
        
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not logged in");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            PaymentDetailsResponse details = paymentService.getPaymentDetails(userId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("Error getting payment details", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get payment details: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Process payment success
     * Called by frontend when payment is detected as successful, updates user membership status
     */
    @PostMapping("/process-success")
    public ResponseEntity<?> processSuccess(@RequestBody Map<String, String> request, HttpSession session) {
        String paymentIntentId = request.get("paymentIntentId");
        if (paymentIntentId == null) {
            paymentIntentId = (String) session.getAttribute("paymentIntentId");
        }
        
        String userId = (String) session.getAttribute("userId");
        
        if (paymentIntentId == null || userId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Missing required payment information or user not logged in");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            boolean success = paymentService.processPaymentSuccess(paymentIntentId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            if (success) {
                response.put("message", "Payment processed successfully");
            } else {
                response.put("message", "Payment processing failed");
            }
            
            return ResponseEntity.ok(response);
        } catch (PaymentProcessingException e) {
            log.error("Error processing payment success callback", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Cancel subscription
     * User subscription cancellation functionality
     */
    @PostMapping("/cancel-subscription")
    public ResponseEntity<?> cancelSubscription(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        
        if (userId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not logged in");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            boolean cancelled = paymentService.cancelSubscription(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", cancelled);
            if (cancelled) {
                response.put("message", "Subscription cancelled");
            } else {
                response.put("message", "No active subscription found");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cancelling subscription", e);
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to cancel subscription: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 检查事件是否已处理
     */
    private boolean hasProcessedEvent(String eventId) {
        return processedEvents.contains(eventId);
    }
    
    /**
     * 标记事件已处理
     */
    private void markEventAsProcessed(String eventId) {
        processedEvents.add(eventId);
        // 如果是生产环境，应该持久化这个记录到数据库
        // 并可能设置过期时间或定期清理机制
    }
    
    /**
     * 从PaymentIntent中获取用户ID
     * 可以从metadata或通过paymentIntentId查询数据库
     */
    private String getUserIdFromPaymentIntent(PaymentIntent paymentIntent) {
        // 方法1：从metadata中获取
        if (paymentIntent.getMetadata() != null && paymentIntent.getMetadata().containsKey("userId")) {
            return paymentIntent.getMetadata().get("userId");
        }
        
        // 方法2：通过paymentIntentId查询支付记录获取userId
        try {
            // 查询支付记录
            String paymentIntentId = paymentIntent.getId();
            Payment payment = paymentService.findByPaymentIntentId(paymentIntentId);
            if (payment != null && payment.getUserId() != null) {
                return payment.getUserId();
            }
        } catch (Exception e) {
            log.error("Error getting userId from payment record", e);
        }
        
        return null;
    }
} 