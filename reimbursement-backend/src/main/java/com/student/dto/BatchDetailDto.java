package com.student.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class BatchDetailDto {
    @NotNull(message = "项目ID不能为空")
    private Long projectId;
    
    @Valid
    private List<ReimbursementDetailDto> details;
}