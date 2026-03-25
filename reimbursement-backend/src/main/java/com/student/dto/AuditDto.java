package com.student.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class AuditDto {
    @NotNull(message = "审批对象ID不能为空")
    private Long targetId;        // 项目ID或明细ID
    
    @NotNull(message = "审批类型不能为空")
    private String auditType;      // PROJECT(项目审批) / DETAIL(明细审批)
    
    @NotNull(message = "审批结果不能为空")
    private Integer result;        // 1:通过 2:驳回
    
    private String comment;        // 审批意见（驳回时必填）
}