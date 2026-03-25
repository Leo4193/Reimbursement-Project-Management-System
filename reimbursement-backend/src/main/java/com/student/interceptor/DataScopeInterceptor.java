package com.student.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.student.annotation.DataScope;
import com.student.utils.SecurityUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

@Component
public class DataScopeInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, 
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        try {
            // 获取当前用户
            Long userId = SecurityUtils.getCurrentUserId();
            String role = SecurityUtils.getCurrentUserRole();
            
            if (userId == null) {
                return;
            }

            // 管理员可以看所有数据
            if ("A".equals(role)) {
                return;
            }

            // 获取方法上的DataScope注解
            DataScope dataScope = getDataScope(ms);
            if (dataScope == null || !dataScope.required()) {
                return;
            }

            // 构建权限SQL
            String sql = boundSql.getSql();
            String newSql = buildDataScopeSql(sql, dataScope, userId, role);
            
            // 通过反射修改SQL
            PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
            mpBs.sql(newSql);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildDataScopeSql(String sql, DataScope dataScope, Long userId, String role) {
        StringBuilder scopeSql = new StringBuilder();
        
        if ("B".equals(role)) {
            // 部门负责人：看本部门
            scopeSql.append(" AND ").append(dataScope.deptAlias())
                    .append(" IN (SELECT department_id FROM sys_user WHERE id = ").append(userId).append(")");
        } else if ("C".equals(role)) {
            // 项目主管：只看自己创建的
            scopeSql.append(" AND ").append(dataScope.userAlias()).append(" = ").append(userId);
        }

        if (scopeSql.length() > 0) {
            // 在WHERE条件后添加
            if (sql.toLowerCase().contains("where")) {
                sql = sql + scopeSql.toString();
            } else {
                sql = sql + " WHERE 1=1 " + scopeSql.toString();
            }
        }
        
        return sql;
    }

    private DataScope getDataScope(MappedStatement ms) {
        try {
            String id = ms.getId();
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1);
            
            Class<?> clazz = Class.forName(className);
            Method[] methods = clazz.getDeclaredMethods();
            
            for (Method method : methods) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(DataScope.class)) {
                    return method.getAnnotation(DataScope.class);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}