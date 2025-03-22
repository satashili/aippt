package com.aippt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;

/**
 * 环境变量配置类，用于加载.env文件中的环境变量
 */
@Configuration
public class EnvConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);
    
    /**
     * 在应用启动时加载.env文件中的环境变量
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        logger.info("正在尝试加载.env文件中的环境变量...");
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        
        // 尝试在多个可能的位置查找.env文件
        File envFile = findEnvFile();
        
        if (envFile != null && envFile.exists()) {
            logger.info("找到.env文件: {}", envFile.getAbsolutePath());
            configurer.setLocation(new FileSystemResource(envFile));
            
            // 加载.env文件中的环境变量到系统环境变量中
            try {
                Dotenv dotenv = Dotenv.configure()
                        .directory(envFile.getParent())
                        .filename(envFile.getName())
                        .load();
                
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
                logger.info(".env文件中的环境变量已成功加载到系统中");
            } catch (Exception e) {
                logger.error("加载.env文件出错: " + e.getMessage(), e);
            }
        } else {
            logger.warn("未找到.env文件，将使用application.yml中的默认值");
        }
        
        return configurer;
    }
    
    /**
     * 在不同的位置查找.env文件
     * 按优先级顺序：当前目录、项目根目录、backend目录、config目录
     */
    private static File findEnvFile() {
        // 可能的.env文件位置
        String[] possibleLocations = {
            ".",                  // 当前目录
            "..",                 // 上级目录
            "backend",            // backend目录
            "../backend",         // 相对于运行目录的backend目录
            "config",             // config目录
            "src/main/resources", // 资源目录
            "../config"           // 相对于运行目录的config目录
        };
        
        // 当前目录的绝对路径（用于日志记录）
        String currentDir = Paths.get("").toAbsolutePath().toString();
        logger.info("当前工作目录: {}", currentDir);
        
        // 在各个可能的位置查找.env文件
        for (String location : possibleLocations) {
            File envFile = new File(location, ".env");
            logger.info("正在检查位置: {}", envFile.getAbsolutePath());
            
            if (envFile.exists() && envFile.isFile()) {
                return envFile;
            }
        }
        
        // 如果都没找到，返回null
        return null;
    }
} 