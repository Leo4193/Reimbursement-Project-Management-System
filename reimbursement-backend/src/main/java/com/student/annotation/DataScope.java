package com.student.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    // 部门ID的字段名
    String deptAlias() default "department_id";
    
    // 用户ID的字段名
    String userAlias() default "creator_id";
    
    // 是否必须检查
    boolean required() default true;
}