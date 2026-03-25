package com.student.security;

import com.student.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // 添加请求路径日志
        System.out.println("=== JwtAuthenticationFilter ===");
        System.out.println("请求路径: " + request.getRequestURI());
        System.out.println("请求方法: " + request.getMethod());
        
        try {
            String jwt = parseJwt(request);
            System.out.println("提取的token: " + (jwt != null ? jwt.substring(0, Math.min(20, jwt.length())) + "..." : "null"));
            
            if (jwt != null) {
                System.out.println("Token验证结果: " + jwtUtils.validateToken(jwt));
                
                if (jwtUtils.validateToken(jwt)) {
                    String username = jwtUtils.getUsernameFromToken(jwt);
                    System.out.println("Token中的用户名: " + username);
                    
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.println("用户权限: " + userDetails.getAuthorities());
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("认证成功，已设置SecurityContext");
                } else {
                    System.out.println("Token验证失败");
                }
            } else {
                System.out.println("请求中没有token");
            }
        } catch (Exception e) {
            System.out.println("Token处理异常: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("继续过滤器链");
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        System.out.println("Authorization头: " + headerAuth);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}