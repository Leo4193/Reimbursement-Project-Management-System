package com.student.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExportDepartmentDto {
    @ExcelProperty("部门名称")
    @ColumnWidth(20)
    private String departmentName;
    
    @ExcelProperty("报销总额(元)")
    @ColumnWidth(15)
    private BigDecimal totalAmount;
    
    @ExcelProperty("明细数量")
    @ColumnWidth(10)
    private Integer detailCount;
    
    @ExcelProperty("项目数量")
    @ColumnWidth(10)
    private Integer projectCount;
    
    @ExcelProperty("平均金额(元)")
    @ColumnWidth(15)
    private BigDecimal avgAmount;
}