package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict")
public class SysDict {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String dictType;     // 字典类型
    private String dictCode;     // 字典编码
    private String dictValue;    // 字典值
    private Integer sortOrder;   // 排序
    private Integer status;      // 状态 1:启用 0:禁用
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}