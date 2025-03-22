package com.aippt.service;

import com.aippt.dto.payment.CreatePaymentIntentRequest;
import com.aippt.dto.payment.CreatePaymentIntentResponse;
import com.aippt.dto.payment.PaymentDetailsResponse;
import com.aippt.entity.Payment;

public interface PaymentService {

    /**
     * 创建支付意向
     * 
     * @param request 支付请求信息
     * @return 包含客户端密钥的响应
     */
    CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request);
    
    /**
     * 处理支付成功后的回调
     * 
     * @param paymentIntentId Stripe支付意向ID
     * @param userId 用户ID
     * @return 是否处理成功
     */
    boolean processPaymentSuccess(String paymentIntentId, String userId);
    
    /**
     * 获取用户的最近一笔支付详情
     * 
     * @param userId 用户ID
     * @return 支付详情
     */
    PaymentDetailsResponse getPaymentDetails(String userId);
    
    /**
     * 取消用户订阅
     * 
     * @param userId 用户ID
     * @return 是否取消成功
     */
    boolean cancelSubscription(String userId);

    Payment findByPaymentIntentId(String paymentIntentId);
} 