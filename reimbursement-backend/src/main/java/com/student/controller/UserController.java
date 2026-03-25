package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.User;
import com.student.mapper.UserMapper;
import com.student.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.student.constant.DepartmentConstants;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController extends BaseController {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        User user = userMapper.selectById(userId);
        user.setPassword(null);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("studentNo", user.getStudentNo());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("roleCode", user.getRoleCode());
        result.put("departmentId", user.getDepartmentId());
        
        // 添加部门名称
        if (user.getDepartmentId() != null) {
            result.put("departmentName", DepartmentConstants.getDepartmentName(user.getDepartmentId()));
        }
        
        return Result.success(result);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody User user, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        User existUser = userMapper.selectById(userId);
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        
        existUser.setRealName(user.getRealName());
        existUser.setStudentNo(user.getStudentNo());
        existUser.setEmail(user.getEmail());
        existUser.setPhone(user.getPhone());
        existUser.setDepartmentId(user.getDepartmentId());
        existUser.setUpdateTime(LocalDateTime.now());
        
        userMapper.updateById(existUser);
        return Result.success("更新成功");
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("请填写完整信息");
        }
        
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success("密码修改成功，请重新登录");
    }

    /**
     * 分页查询用户列表（管理员）
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        
        // 只有管理员可以查看用户列表
        String role = getCurrentUserRole(request);
        if (!"A".equals(role)) {
            return Result.forbidden("无权查看");
        }
        
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getRealName, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        
        userMapper.selectPage(page, wrapper);
        
        // 隐藏密码
        page.getRecords().forEach(u -> u.setPassword(null));
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        
        return Result.success(result);
    }

    /**
     * 新增用户（管理员）
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody User user, HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        if (!"A".equals(role)) {
            return Result.forbidden("无权操作");
        }
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        
        return Result.success("新增成功");
    }

    /**
     * 更新用户信息（管理员）
     */
    @PutMapping("/admin/update")
    public Result<String> updateByAdmin(@RequestBody User user, HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        if (!"A".equals(role)) {
            return Result.forbidden("无权操作");
        }
        
        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        
        existUser.setRealName(user.getRealName());
        existUser.setStudentNo(user.getStudentNo());
        existUser.setEmail(user.getEmail());
        existUser.setPhone(user.getPhone());
        existUser.setDepartmentId(user.getDepartmentId());
        existUser.setRoleCode(user.getRoleCode());
        existUser.setUpdateTime(LocalDateTime.now());
        
        userMapper.updateById(existUser);
        return Result.success("更新成功");
    }

    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        if (!"A".equals(role)) {
            return Result.forbidden("无权操作");
        }
        
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }
}