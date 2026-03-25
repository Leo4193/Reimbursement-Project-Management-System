package com.student.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ModifyAuditResultDto {
    @NotNull(message = "明细ID不能为空")
    private Long detailId;
    
    @NotNull(message = "修改后的状态不能为空")
    private Integer newStatus;      // 1:已通过 2:已驳回 0:待审批
    
    private String comment;         // 修改原因
}