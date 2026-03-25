package com.student.security;

import com.student.entity.User;
import com.student.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("正在加载用户: " + username);
        
        User user = userMapper.findByUsername(username);
        if (user == null) {
            System.out.println("用户不存在: " + username);
            throw new UsernameNotFoundException("用户不存在");
        }
        
        System.out.println("找到用户: " + user.getUsername());
        System.out.println("用户角色: " + user.getRoleCode());
        System.out.println("用户状态: " + user.getStatus());
        
        // 根据角色代码设置权限
        String role = "ROLE_" + user.getRoleCode();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}