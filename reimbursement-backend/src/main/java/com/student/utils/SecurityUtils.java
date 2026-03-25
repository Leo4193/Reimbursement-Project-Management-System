package com.student.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        // 从Authentication的details中获取，需要根据实际情况调整
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() != null) {
            try {
                return Long.parseLong(authentication.getDetails().toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().iterator().next()
                    .getAuthority().replace("ROLE_", "");
        }
        return null;
    }

    /**
     * 是否管理员
     */
    public static boolean isAdmin() {
        return "A".equals(getCurrentUserRole());
    }

    /**
     * 是否部门负责人
     */
    public static boolean isDepartmentLeader() {
        return "B".equals(getCurrentUserRole());
    }

    /**
     * 是否项目主管
     */
    public static boolean isProjectLeader() {
        return "C".equals(getCurrentUserRole());
    }
}