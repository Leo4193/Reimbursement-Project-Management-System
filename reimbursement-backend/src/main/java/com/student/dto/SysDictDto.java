package com.student.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class SysDictDto {
    private Long id;
    
    @NotBlank(message = "字典类型不能为空")
    private String dictType;
    
    @NotBlank(message = "字典编码不能为空")
    private String dictCode;
    
    @NotBlank(message = "字典值不能为空")
    private String dictValue;
    
    private Integer sortOrder = 0;
    
    private Integer status = 1;
}