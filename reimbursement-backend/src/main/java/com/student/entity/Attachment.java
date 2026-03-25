package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("attachment")
public class Attachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;        // 项目ID
    
    private Long detailId;          // 报销明细ID
    
    private String fileName;        // 文件名
    
    private String filePath;        // 文件路径
    
    private Long fileSize;          // 文件大小(字节)
    
    private String fileType;        // 文件类型
    
    private Long uploadBy;           // 上传人ID
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
}