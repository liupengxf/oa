package org.buu.oa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置跨域、资源路径等Web相关设置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域访问规则
     * 允许前端跨域请求后端API
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // 允许所有来源
            .allowedOriginPatterns("*")
            // 允许的HTTP方法
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            // 允许的请求头
            .allowedHeaders("*")
            // 是否允许携带凭证
            .allowCredentials(true)
            // 预检请求缓存时间（秒）
            .maxAge(3600);
    }
}