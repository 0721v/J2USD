package com.giftcard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 静态资源 - 公开访问
                .requestMatchers("/").permitAll()
                .requestMatchers("/index.html").permitAll()
                .requestMatchers("/pages/**").permitAll()
                .requestMatchers("/admin/**").permitAll()  // admin HTML 页面由前端 JS 控制跳转
                .requestMatchers("/assets/**").permitAll()
                // 公开 API（前台）
                .requestMatchers("/api/site-settings").permitAll()
                .requestMatchers("/api/products").permitAll()
                .requestMatchers("/api/products/{id}").permitAll()
                .requestMatchers("/api/categories").permitAll()
                .requestMatchers("/api/orders", "/api/orders/**").permitAll()
                .requestMatchers("/api/translate/**").permitAll()
                // 管理后台 - 登录和初始化接口公开
                .requestMatchers("/api/admin/login").permitAll()
                .requestMatchers("/api/admin/init").permitAll()
                .requestMatchers("/api/admin/payment-config/status").permitAll()
                .requestMatchers("/api/admin/payment-config/trc20/public").permitAll()
                .requestMatchers("/api/admin/payment-config/okx/public").permitAll()
                .requestMatchers("/api/admin/check-auth").permitAll()
                .requestMatchers("/error").permitAll()
                // 其他所有请求需要认证（包括 /api/admin/** 除上面公开的）
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
