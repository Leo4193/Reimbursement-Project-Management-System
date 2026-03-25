package com.student.controller;

import com.student.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseController {
    
    @Autowired
    protected TokenUtils tokenUtils;
    
    /**
     * 获取当前用户ID
     */
    protected Long getCurrentUserId(HttpServletRequest request) {
        return tokenUtils.getCurrentUserId(request);
    }
    
    /**
     * 获取当前用户名
     */
    protected String getCurrentUsername(HttpServletRequest request) {
        return tokenUtils.getCurrentUsername(request);
    }
    
    /**
     * 获取当前用户角色
     */
    protected String getCurrentUserRole(HttpServletRequest request) {
        return tokenUtils.getCurrentUserRole(request);
    }
    
    /**
     * 检查是否管理员
     */
    protected boolean isAdmin(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "A".equals(role);
    }
    
    /**
     * 检查是否部门负责人
     */
    protected boolean isDepartmentLeader(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "B".equals(role);
    }
    
    /**
     * 检查是否项目主管
     */
    protected boolean isProjectLeader(HttpServletRequest request) {
        String role = getCurrentUserRole(request);
        return "C".equals(role);
    }
}