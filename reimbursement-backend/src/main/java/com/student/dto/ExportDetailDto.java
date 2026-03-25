package com.student.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExportDetailDto {
    @ExcelProperty("项目编号")
    @ColumnWidth(15)
    private String projectNo;
    
    @ExcelProperty("项目名称")
    @ColumnWidth(20)
    private String projectName;
    
    @ExcelProperty("部门")
    @ColumnWidth(15)
    private String department;
    
    @ExcelProperty("经办人")
    @ColumnWidth(12)
    private String agent;
    
    @ExcelProperty("学号")
    @ColumnWidth(15)
    private String studentNo;
    
    @ExcelProperty("内容")
    @ColumnWidth(25)
    private String content;
    
    @ExcelProperty("单价")
    @ColumnWidth(10)
    private BigDecimal unitPrice;
    
    @ExcelProperty("数量")
    @ColumnWidth(8)
    private Integer quantity;
    
    @ExcelProperty("总金额")
    @ColumnWidth(12)
    private BigDecimal totalAmount;
    
    @ExcelProperty("发票号码")
    @ColumnWidth(15)
    private String invoiceNo;
    
    @ExcelProperty("发票类型")
    @ColumnWidth(12)
    private String invoiceType;
    
    @ExcelProperty("报销类别")
    @ColumnWidth(12)
    private String reimbursementCategory;
    
    @ExcelProperty("金额类别")
    @ColumnWidth(15)
    private String amountCategory;
    
    @ExcelProperty("汇款方式")
    @ColumnWidth(10)
    private String paymentMethod;
    
    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusName;
    
    @ExcelProperty("创建时间")
    @ColumnWidth(20)
    private String createTime;
}