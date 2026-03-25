package com.student.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TokenUtils {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 从请求中提取token
     */
    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 从请求中获取当前用户ID
     */
    public Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null && jwtUtils.validateToken(token)) {
            return jwtUtils.getUserIdFromToken(token);
        }
        return null;
    }
    
    /**
     * 从请求中获取当前用户名
     */
    public String getCurrentUsername(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null && jwtUtils.validateToken(token)) {
            return jwtUtils.getUsernameFromToken(token);
        }
        return null;
    }
    
    /**
     * 从请求中获取当前用户角色
     */
    public String getCurrentUserRole(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null && jwtUtils.validateToken(token)) {
            return jwtUtils.getRoleFromToken(token);
        }
        return null;
    }
}