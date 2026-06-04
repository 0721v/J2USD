package com.giftcard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giftcard.common.Result;
import com.giftcard.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 不拦截公开路径
        return path.startsWith("/pages/")
            || path.startsWith("/assets/")
            || path.equals("/")
            || path.equals("/index.html")
            || path.startsWith("/api/products") && !path.startsWith("/api/admin/products")
            || path.startsWith("/api/orders") && !path.startsWith("/api/admin/orders")
            || path.startsWith("/api/site-settings")
            || path.startsWith("/api/translate")
            || path.startsWith("/api/admin/login")
            || path.startsWith("/api/admin/init")
            || path.startsWith("/api/admin/payment-config/status")
            || path.startsWith("/api/admin/payment-config/trc20/public")
            || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 对于 /api/admin/** 路径进行 JWT 验证
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/")) {
            String token = extractToken(request);

            if (token == null) {
                sendUnauthorized(response, "未登录，请先登录");
                return;
            }

            try {
                jwtUtil.verifyToken(token);
                Long adminId = jwtUtil.getAdminIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
            } catch (Exception e) {
                sendUnauthorized(response, "登录已过期，请重新登录");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 也支持从 header 中直接获取 token
        String token = request.getHeader("X-Admin-Token");
        if (token != null && !token.isEmpty()) {
            return token;
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        new ObjectMapper().writeValue(response.getOutputStream(), result);
    }
}
