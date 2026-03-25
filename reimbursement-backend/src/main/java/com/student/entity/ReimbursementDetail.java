package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reimbursement_detail")
public class ReimbursementDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String detailNo;
    
    private String department;
    
    private String agent;
    
    private String studentNo;
    
    private String content;
    
    private BigDecimal unitPrice;
    
    private Integer quantity;
    
    private BigDecimal totalAmount;
    
    private String invoiceNo;
    
    private String invoiceType;
    
    private String reimbursementCategory;
    
    private String amountCategory;
    
    private String paymentMethod;
    
    private String remark;
    
    private Integer status;
    
    private Long creatorId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}