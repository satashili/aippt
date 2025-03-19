package com.aippt.config;

import com.aippt.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Arrays;

/**
 * Spring Security配置类
 * 负责配置应用程序的安全性，包括认证、授权、CORS等设置
 */
@Configuration // 标识这是一个配置类，会被Spring容器识别并处理
@EnableWebSecurity // 启用Spring Security的Web安全支持
public class SecurityConfig {

    /**
     * 自定义的OAuth2用户服务，用于处理从Google获取的用户信息
     */
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    /**
     * 配置安全过滤链
     * 这是Spring Security 5.7+的新配置方式，替代了旧版本的WebSecurityConfigurerAdapter
     *
     * @param http HttpSecurity对象，用于构建安全配置
     * @return 构建好的SecurityFilterChain
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 配置CORS（跨域资源共享）
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 禁用CSRF（跨站请求伪造）保护
            // 使用方法引用语法，更简洁
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置请求授权规则
            .authorizeHttpRequests(authorize -> authorize
                // 允许这些路径无需认证即可访问
                .requestMatchers("/", "/error", "/webjars/**").permitAll()
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            
            // 配置OAuth2登录
            .oauth2Login(oauth2 -> oauth2
                // 配置用户信息端点
                .userInfoEndpoint(userInfo -> userInfo
                    // 使用自定义的OAuth2用户服务处理用户信息
                    .userService(customOAuth2UserService)
                )
                // 登录成功后重定向到这个URL
                .defaultSuccessUrl("/loginSuccess", true)
                // 登录失败后重定向到这个URL
                .failureUrl("/loginFailure")
                // 允许所有用户访问登录相关端点
                .permitAll()
            )
            
            // 配置异常处理
            .exceptionHandling(exceptions -> exceptions
                // 当未认证用户尝试访问需要认证的资源时，返回401状态码
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );
        
        // 构建并返回配置好的安全过滤链
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