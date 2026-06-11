package com.giftcard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传文件目录
        Path uploadPath = Paths.get("./uploads").toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 设置首页
        registry.addViewController("/").setViewName("forward:/index.html");
        
        // /admin 转发到登录页（保持原域名）
        registry.addViewController("/admin").setViewName("forward:/admin/login.html");
        
        // 页面路由转发 - 处理前端路由刷新问题
        registry.addViewController("/pages/{page:[^.]*}").setViewName("forward:/pages/{page}.html");
        registry.addViewController("/admin/{page:[^.]*}").setViewName("forward:/admin/{page}.html");
    }
}
