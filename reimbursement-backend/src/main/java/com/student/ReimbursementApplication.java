package com.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReimbursementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReimbursementApplication.class, args);
        System.out.println("========================================");
        System.out.println("报销管理系统后端启动成功！");
        System.out.println("访问地址：http://localhost:8080");
        System.out.println("========================================");
    }
}