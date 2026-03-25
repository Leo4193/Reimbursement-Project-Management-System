package com.student.controller;

import com.student.dto.LoginDto;
import com.student.dto.LoginResponseDto;
import com.student.dto.RegisterDto;
import com.student.entity.User;
import com.student.mapper.UserMapper;
import com.student.utils.JwtUtils;
import com.student.utils.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDto) {
        try {
            System.out.println("=== 登录尝试 ===");
            System.out.println("用户名: " + loginDto.getUsername());
            
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            User user = userMapper.findByUsername(loginDto.getUsername());
            System.out.println("用户信息: ID=" + user.getId() + ", 角色=" + user.getRoleCode());
            
            // 生成token（包含用户ID）
            String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
            
            // 构建响应
            LoginResponseDto response = LoginResponseDto.builder()
                    .token(token)
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .roleCode(user.getRoleCode())
                    .userId(user.getId())
                    .build();
            
            return Result.success("登录成功", response);
        } catch (Exception e) {
            System.out.println("登录失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDto registerDto) {
        // 检查用户名是否已存在
        User existUser = userMapper.findByUsername(registerDto.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setStudentNo(registerDto.getStudentNo());
        user.setEmail(registerDto.getEmail());
        user.setRealName(registerDto.getRealName());
        user.setPhone(registerDto.getPhone());
        user.setRoleCode("C");  // 默认角色：项目主管
        user.setStatus(1);       // 启用
        user.setCreateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        return Result.success("注册成功");
    }

    /**
     * 登出接口
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestAttribute(required = false) Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        User user = userMapper.selectById(userId);
        user.setPassword(null); // 隐藏密码
        return Result.success(user);
    }
}