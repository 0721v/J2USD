package com.giftcard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 设置首页
        registry.addViewController("/").setViewName("forward:/index.html");
        
        // /admin 自动跳转到登录页
        registry.addRedirectViewController("/admin", "/admin/login.html");
        
        // 页面路由转发 - 处理前端路由刷新问题
        registry.addViewController("/pages/{page:[^.]*}").setViewName("forward:/pages/{page}.html");
        registry.addViewController("/admin/{page:[^.]*}").setViewName("forward:/admin/{page}.html");
    }
}
