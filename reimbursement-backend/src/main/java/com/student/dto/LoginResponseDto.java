package com.student.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String token;
    private String username;
    private String realName;
    private String roleCode;
    private Long userId;
}