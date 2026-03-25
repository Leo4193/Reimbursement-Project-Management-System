package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_record")
public class AuditRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;           // 项目ID
    
    private Long detailId;             // 报销明细ID
    
    private Long auditorId;            // 审核人ID
    
    private String auditorName;        // 审核人姓名
    
    private String auditorRole;        // 审核人角色
    
    private String operationType;      // 操作类型：PROJECT_AUDIT(项目审批)/DETAIL_AUDIT(明细审批)/MODIFY_RESULT(修改结果)
    
    private Integer beforeStatus;      // 修改前状态
    
    private Integer afterStatus;       // 修改后状态
    
    private String comment;            // 审核意见
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}