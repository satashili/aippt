package com.aippt.config;

import com.aippt.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;

import java.util.Arrays;

/**
 * Spring Security配置类
 * 负责配置应用程序的安全性，包括认证、授权、CORS等设置
 */
@Configuration // 标识这是一个配置类，会被Spring容器识别并处理
@EnableWebSecurity // 启用Spring Security的Web安全支持
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);


    /**
     * 配置安全过滤链
     * 这是Spring Security 5.7+的新配置方式，替代了旧版本的WebSecurityConfigurerAdapter
     *
     * @param http HttpSecurity对象，用于构建安全配置
     * @param customOAuth2UserService CustomOAuth2UserService实例
     * @return 构建好的SecurityFilterChain
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        logger.info("====================================================================");
        logger.info("Configuring SecurityFilterChain");
        logger.info("Injected CustomOAuth2UserService instance: {}", customOAuth2UserService);
        
        
        http
            // 配置CORS（跨域资源共享）
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 禁用CSRF（跨站请求伪造）保护
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置请求授权规则
            .authorizeHttpRequests(authorize -> {
                logger.info("Configuring authorization rules");
                authorize
                    // 允许这些路径无需认证即可访问
                    .requestMatchers("/", "/error", "/webjars/**", "/login/**", "/api/auth/register", "/api/auth/login", "/api/auth/verify-email", "/api/auth/resend-verification", "/api/test/**").permitAll()
                    // 其他所有请求都需要认证
                    .anyRequest().authenticated();
            })
            
            // 配置OAuth2登录
            .oauth2Login(oauth2 -> {
                logger.info("====================================================================");
                logger.info("Configuring OAuth2 login");
                logger.info("Using CustomOAuth2UserService instance in oauth2Login: {}", customOAuth2UserService);
                
                oauth2
                    .userInfoEndpoint(userInfo -> {
                        logger.info("Configuring userInfoEndpoint");
                        logger.info("Setting CustomOAuth2UserService as userService");
                        userInfo.userService(customOAuth2UserService);
                        logger.info("UserInfoEndpoint configured with CustomOAuth2UserService");
                    })
                    .successHandler((request, response, authentication) -> {
                        logger.info("====================================================================");
                        logger.info("OAuth2 login successful");
                        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                        logger.info("Authenticated user details:");
                        logger.info("Name: {}", oauth2User.getName());
                        logger.info("Attributes: {}", oauth2User.getAttributes());
                        logger.info("Authorities: {}", oauth2User.getAuthorities());
                        logger.info("Redirecting to frontend with success status");
                        logger.info("====================================================================");
                        response.sendRedirect("http://localhost:3000?loginSuccess=true");
                    })
                    .failureHandler((request, response, exception) -> {
                        logger.error("====================================================================");
                        logger.error("OAuth2 login failed");
                        logger.error("Error type: {}", exception.getClass().getName());
                        logger.error("Error message: {}", exception.getMessage());
                        logger.error("Stack trace:", exception);
                        logger.error("Request details:");
                        logger.error("URI: {}", request.getRequestURI());
                        logger.error("Query string: {}", request.getQueryString());
                        logger.error("Redirecting to frontend with error status");
                        logger.error("====================================================================");
                        response.sendRedirect("http://localhost:3000?error=true");
                    });
                
                logger.info("OAuth2 login configuration completed");
                logger.info("====================================================================");
            })
            
            // 添加登出配置
            .logout(logout -> logout
                .logoutSuccessUrl("http://localhost:3000?logoutSuccess=true")
                .permitAll()
            )
            
            // 配置异常处理
            .exceptionHandling(exceptions -> exceptions
                // 当未认证用户尝试访问需要认证的资源时，返回401状态码
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );
        
        logger.info("SecurityFilterChain configuration completed");
        logger.info("====================================================================");
        
        return http.build();
    }

    /**
     * 配置CORS（跨域资源共享）
     * 允许前端应用从不同的域访问后端API
     *
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 创建CORS配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        // 设置允许的源（域名），这里允许前端地址http://localhost:3000访问
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        // 设置允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 设置允许的请求头，*表示允许所有头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 设置是否允许发送Cookie
        configuration.setAllowCredentials(true);
        
        // 创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 为所有路径应用这个CORS配置
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 