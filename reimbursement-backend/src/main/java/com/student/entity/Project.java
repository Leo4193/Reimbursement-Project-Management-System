package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String projectNo;
    private String name;
    private Integer typeId;
    // 删除 departmentId 字段，改为多部门关联表
    private String projectLeader;
    private BigDecimal budgetAmount;
    private LocalDateTime applyTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Long creatorId;
    
    private String departmentFinanceLeader;
    private String officeFinanceLeader;
    private String activityChairperson;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}