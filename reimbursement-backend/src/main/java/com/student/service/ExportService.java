package com.student.service;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

public interface ExportService {
    
    // 导出报销明细Excel
    void exportDetailsExcel(Long projectId, HttpServletResponse response) throws IOException;
    
    // 导出部门统计Excel
    void exportDepartmentStatisticsExcel(HttpServletResponse response) throws IOException;
    
    // 导出项目统计Excel
    void exportProjectStatisticsExcel(HttpServletResponse response) throws IOException;
    
    // 导出报销明细PDF
    void exportDetailsPdf(Long projectId, HttpServletResponse response) throws IOException;
    
    // 导出项目附件压缩包
    void exportProjectAttachmentsZip(Long projectId, HttpServletResponse response) throws IOException;
}