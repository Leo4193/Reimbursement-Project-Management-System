package com.student.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TimeStatisticsDto {
    private String year;
    private String month;
    private String day;
    private BigDecimal amount;
    private Integer count;
}