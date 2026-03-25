package com.student.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class ReimbursementDetailDto {
    private Long id;
    
    //@NotNull(message = "项目ID不能为空")
    //private Long projectId;
    
    @NotBlank(message = "部门不能为空")
    private String department;
    
    @NotBlank(message = "经办人不能为空")
    private String agent;
    
    @NotBlank(message = "学号不能为空")
    private String studentNo;
    
    @NotBlank(message = "内容不能为空")
    private String content;
    
    @NotNull(message = "单价不能为空")
    @Positive(message = "单价必须大于0")
    private BigDecimal unitPrice;
    
    @NotNull(message = "数量不能为空")
    @Positive(message = "数量必须大于0")
    private Integer quantity;
    
    @NotBlank(message = "发票号码不能为空")
    private String invoiceNo;
    
    @NotBlank(message = "发票类型不能为空")
    private String invoiceType;
    
    @NotBlank(message = "报销类别不能为空")
    private String reimbursementCategory;
    
    @NotBlank(message = "金额类别不能为空")
    private String amountCategory;
    
    @NotBlank(message = "汇款方式不能为空")
    private String paymentMethod;
    
    private String remark;
}