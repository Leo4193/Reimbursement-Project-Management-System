package com.student.controller;

import com.student.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/hello")
    public Result<Map<String, String>> hello() {
        Map<String, String> data = new HashMap<>();
        data.put("message", "Hello, 报销管理系统!");
        data.put("time", String.valueOf(System.currentTimeMillis()));
        return Result.success(data);
    }
}