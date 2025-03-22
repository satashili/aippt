package com.aippt.service.impl;

import com.aippt.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.email.verification.base-url}")
    private String verificationBaseUrl;
    
    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        logger.info("发送简单邮件到: {}", to);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            logger.info("邮件发送成功");
        } catch (Exception e) {
            logger.error("邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
    
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        logger.info("发送HTML邮件到: {}", to);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("HTML邮件发送成功");
        } catch (MessagingException e) {
            logger.error("HTML邮件发送失败: {}", e.getMessage());
            throw new RuntimeException("HTML邮件发送失败", e);
        }
    }
    
    @Override
    public void sendVerificationEmail(String to, String name, String token) {
        logger.info("发送验证邮件到: {}", to);
        
        String verificationLink = verificationBaseUrl + "?token=" + token;
        String subject = "AISlides - 请验证您的邮箱";
        
        String htmlContent = 
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>" +
            "    <div style='text-align: center; margin-bottom: 20px;'>" +
            "        <h1 style='color: #3498db;'><span style='font-weight: bold;'>AI</span>Slides</h1>" +
            "    </div>" +
            "    <div style='padding: 20px; background-color: #f9f9f9; border-radius: 5px;'>" +
            "        <h2 style='color: #333; margin-top: 0;'>验证您的邮箱</h2>" +
            "        <p>亲爱的 " + name + "，</p>" +
            "        <p>感谢您注册AISlides。请点击下面的按钮验证您的邮箱地址：</p>" +
            "        <div style='text-align: center; margin: 30px 0;'>" +
            "            <a href='" + verificationLink + "' style='background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;'>验证邮箱</a>" +
            "        </div>" +
            "        <p>或者复制下面的链接到浏览器地址栏：</p>" +
            "        <p style='background-color: #eeeeee; padding: 10px; border-radius: 4px; word-break: break-all;'>" + verificationLink + "</p>" +
            "        <p>如果您没有注册AISlides账号，请忽略此邮件。</p>" +
            "    </div>" +
            "    <div style='margin-top: 20px; text-align: center; color: #888; font-size: 12px;'>" +
            "        <p>© 2023 AISlides. 保留所有权利。</p>" +
            "    </div>" +
            "</div>";
        
        sendHtmlEmail(to, subject, htmlContent);
    }
} 