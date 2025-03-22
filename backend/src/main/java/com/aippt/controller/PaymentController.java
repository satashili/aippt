package com.aippt.controller;

import com.aippt.dto.payment.CreatePaymentIntentRequest;
import com.aippt.dto.payment.CreatePaymentIntentResponse;
import com.aippt.dto.payment.PaymentDetailsResponse;
import com.aippt.exception.PaymentProcessingException;
import com.aippt.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${stripe.publishableKey}")
    private String stripePublishableKey;
    
    @Autowired
    private PaymentService paymentService;
    
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
        log.info("Received Stripe webhook callback");
        
        try {
            // Webhook handling logic here...
            // Verify signature, process event types, etc.
            
            return ResponseEntity.ok("Webhook handled successfully");
        } catch (Exception e) {
            log.error("Error processing webhook callback", e);
            return ResponseEntity.badRequest().body("Webhook Error: " + e.getMessage());
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
} 