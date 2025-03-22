package com.aippt.service;

/**
 * 邮件服务接口
 * 提供邮件发送功能
 */
public interface EmailService {
    
    /**
     * 发送普通文本邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendSimpleEmail(String to, String subject, String content);
    
    /**
     * 发送HTML格式邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param htmlContent HTML格式邮件内容
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
    
    /**
     * 发送验证邮件
     *
     * @param to 收件人邮箱
     * @param name 用户名
     * @param token 验证token
     */
    void sendVerificationEmail(String to, String name, String token);
} 