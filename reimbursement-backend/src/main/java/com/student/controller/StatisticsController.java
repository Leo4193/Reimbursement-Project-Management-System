package com.student.controller;

import com.student.dto.DepartmentStatisticsDto;
import com.student.dto.TimeStatisticsDto;
import com.student.service.ExportService;
import com.student.service.StatisticsService;
import com.student.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "http://localhost:3000")
public class StatisticsController extends BaseController {

    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private ExportService exportService;

    /**
     * 按部门统计
     */
    @GetMapping("/department")
    public Result<List<DepartmentStatisticsDto>> getDepartmentStatistics() {
        List<DepartmentStatisticsDto> result = statisticsService.getDepartmentStatistics();
        return Result.success(result);
    }

    /**
     * 按时间统计
     * period: year, month, day
     */
    @GetMapping("/time")
    public Result<List<TimeStatisticsDto>> getTimeStatistics(@RequestParam(defaultValue = "month") String period) {
        List<TimeStatisticsDto> result = statisticsService.getTimeStatistics(period);
        return Result.success(result);
    }

    /**
     * 按状态统计
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatusStatistics() {
        Map<String, Object> result = statisticsService.getStatusStatistics();
        return Result.success(result);
    }

    /**
     * 按项目统计
     */
    @GetMapping("/project")
    public Result<List<Map<String, Object>>> getProjectStatistics() {
        List<Map<String, Object>> result = statisticsService.getProjectStatistics();
        return Result.success(result);
    }

    /**
     * 部门排名
     */
    @GetMapping("/department/ranking")
    public Result<List<DepartmentStatisticsDto>> getDepartmentRanking() {
        List<DepartmentStatisticsDto> result = statisticsService.getDepartmentRanking();
        return Result.success(result);
    }

    /**
     * 审核通过率
     */
    @GetMapping("/approval-rate")
    public Result<Map<String, Object>> getApprovalRate() {
        Map<String, Object> result = statisticsService.getApprovalRate();
        return Result.success(result);
    }

    /**
     * 平均处理时长
     */
    @GetMapping("/avg-process-time")
    public Result<Map<String, Object>> getAverageProcessTime() {
        Map<String, Object> result = statisticsService.getAverageProcessTime();
        return Result.success(result);
    }

    /**
     * 导出报销明细Excel
     */
    @GetMapping("/export/details/{projectId}")
    public void exportDetailsExcel(@PathVariable Long projectId, HttpServletResponse response) throws IOException {
        exportService.exportDetailsExcel(projectId, response);
    }

    /**
     * 导出部门统计Excel
     */
    @GetMapping("/export/department")
    public void exportDepartmentExcel(HttpServletResponse response) throws IOException {
        exportService.exportDepartmentStatisticsExcel(response);
    }

    /**
     * 导出项目统计Excel
     */
    @GetMapping("/export/project")
    public void exportProjectExcel(HttpServletResponse response) throws IOException {
        exportService.exportProjectStatisticsExcel(response);
    }

    /**
     * 导出报销明细PDF
     */
    @GetMapping("/export/pdf/{projectId}")
    public void exportDetailsPdf(@PathVariable Long projectId, HttpServletResponse response) throws IOException {
        exportService.exportDetailsPdf(projectId, response);
    }

    /**
     * 导出项目附件压缩包
     */
    @GetMapping("/export/zip/{projectId}")
    public void exportProjectZip(@PathVariable Long projectId, HttpServletResponse response) throws IOException {
        exportService.exportProjectAttachmentsZip(projectId, response);
    }
}