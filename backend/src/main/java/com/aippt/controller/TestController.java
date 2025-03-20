package com.aippt.controller;

import com.aippt.entity.User;
import com.aippt.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UserService userService;

    @GetMapping("/db")
    public Map<String, Object> testDbConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试数据库连接
            Integer count = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("success", true);
            result.put("message", "数据库连接成功");
            result.put("test_result", count);
//            log.info("数据库连接测试成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "数据库连接失败: " + e.getMessage());
//            log.error("数据库连接测试失败", e);
        }
        return result;
    }
    
    @GetMapping("/users/count")
    public Map<String, Object> getUserCount() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 使用MyBatisPlus来测试
            long count = userService.getUserCount();
            result.put("success", true);
            result.put("count", count);
//            log.info("用户表查询成功，共有{}条记录", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询用户表失败: " + e.getMessage());
//            log.error("查询用户表失败", e);
        }
        return result;
    }
} 