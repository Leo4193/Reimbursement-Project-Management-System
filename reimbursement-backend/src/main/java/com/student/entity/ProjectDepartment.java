package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("project_department")
public class ProjectDepartment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Integer departmentId;
}