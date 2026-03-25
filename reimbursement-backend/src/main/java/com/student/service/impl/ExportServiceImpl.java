package com.student.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.student.dto.*;
import com.student.entity.Attachment;
import com.student.entity.Project;
import com.student.entity.ReimbursementDetail;
import com.student.mapper.AttachmentMapper;
import com.student.mapper.ProjectMapper;
import com.student.mapper.ReimbursementDetailMapper;
import com.student.service.ExportService;
import com.student.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExportServiceImpl implements ExportService {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Autowired
    private ReimbursementDetailMapper detailMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private AttachmentMapper attachmentMapper;
    
    @Autowired
    private StatisticsService statisticsService;

    @Override
    public void exportDetailsExcel(Long projectId, HttpServletResponse response) throws IOException {
        // 查询数据
        List<ReimbursementDetail> details = detailMapper.selectByProjectId(projectId);
        Project project = projectMapper.selectById(projectId);
        
        // 转换为导出DTO
        List<ExportDetailDto> exportList = new ArrayList<>();
        for (ReimbursementDetail detail : details) {
            ExportDetailDto dto = new ExportDetailDto();
            dto.setProjectNo(project.getProjectNo());
            dto.setProjectName(project.getName());
            dto.setDepartment(detail.getDepartment());
            dto.setAgent(detail.getAgent());
            dto.setStudentNo(detail.getStudentNo());
            dto.setContent(detail.getContent());
            dto.setUnitPrice(detail.getUnitPrice());
            dto.setQuantity(detail.getQuantity());
            dto.setTotalAmount(detail.getTotalAmount());
            dto.setInvoiceNo(detail.getInvoiceNo());
            dto.setInvoiceType(detail.getInvoiceType());
            dto.setReimbursementCategory(detail.getReimbursementCategory());
            dto.setAmountCategory(detail.getAmountCategory());
            dto.setPaymentMethod(detail.getPaymentMethod());
            
            String statusName;
            switch (detail.getStatus()) {
                case 0: statusName = "待审批"; break;
                case 1: statusName = "已通过"; break;
                case 2: statusName = "已驳回"; break;
                default: statusName = "未知";
            }
            dto.setStatusName(statusName);
            dto.setCreateTime(detail.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            exportList.add(dto);
        }
        
        // 设置响应头
        String fileName = URLEncoder.encode(project.getName() + "_报销明细.xlsx", "UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        // 写入Excel
        EasyExcel.write(response.getOutputStream(), ExportDetailDto.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("报销明细")
                .doWrite(exportList);
    }

    @Override
    public void exportDepartmentStatisticsExcel(HttpServletResponse response) throws IOException {
        List<DepartmentStatisticsDto> stats = statisticsService.getDepartmentStatistics();
        
        List<ExportDepartmentDto> exportList = new ArrayList<>();
        for (DepartmentStatisticsDto stat : stats) {
            ExportDepartmentDto dto = new ExportDepartmentDto();
            dto.setDepartmentName(stat.getDepartmentName());
            dto.setTotalAmount(stat.getTotalAmount());
            dto.setDetailCount(stat.getDetailCount());
            dto.setProjectCount(stat.getProjectCount());
            dto.setAvgAmount(stat.getAvgAmount());
            exportList.add(dto);
        }
        
        String fileName = URLEncoder.encode("部门统计报表.xlsx", "UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        EasyExcel.write(response.getOutputStream(), ExportDepartmentDto.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("部门统计")
                .doWrite(exportList);
    }

    @Override
    public void exportProjectStatisticsExcel(HttpServletResponse response) throws IOException {
        List<java.util.Map<String, Object>> stats = statisticsService.getProjectStatistics();
        
        String fileName = URLEncoder.encode("项目统计报表.xlsx", "UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        EasyExcel.write(response.getOutputStream())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("项目统计")
                .doWrite(stats);
    }

    @Override
    public void exportDetailsPdf(Long projectId, HttpServletResponse response) throws IOException {
        List<ReimbursementDetail> details = detailMapper.selectByProjectId(projectId);
        Project project = projectMapper.selectById(projectId);
        
        String fileName = URLEncoder.encode(project.getName() + "_报销明细.pdf", "UTF-8");
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            
            // 标题
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph(project.getName() + " - 报销明细表", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph("导出时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
            document.add(new Paragraph(" "));
            
            // 表格
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            // 表头
            String[] headers = {"序号", "部门", "经办人", "内容", "单价", "数量", "总金额", "发票号码", "状态", "创建时间"};
            for (String header : headers) {
                table.addCell(new Phrase(header));
            }
            
            // 数据行
            int index = 1;
            for (ReimbursementDetail detail : details) {
                table.addCell(String.valueOf(index++));
                table.addCell(detail.getDepartment());
                table.addCell(detail.getAgent());
                table.addCell(detail.getContent());
                table.addCell(detail.getUnitPrice().toString());
                table.addCell(detail.getQuantity().toString());
                table.addCell(detail.getTotalAmount().toString());
                table.addCell(detail.getInvoiceNo());
                String status = detail.getStatus() == 0 ? "待审批" : (detail.getStatus() == 1 ? "已通过" : "已驳回");
                table.addCell(status);
                table.addCell(detail.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            
            document.add(table);
            
            // 统计信息
            document.add(new Paragraph(" "));
            document.add(new Paragraph("总计：" + details.size() + "条明细"));
            
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    @Override
    public void exportProjectAttachmentsZip(Long projectId, HttpServletResponse response) throws IOException {
        List<Attachment> attachments = attachmentMapper.selectByProjectId(projectId);
        Project project = projectMapper.selectById(projectId);
        
        String fileName = URLEncoder.encode(project.getName() + "_附件.zip", "UTF-8");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (Attachment attachment : attachments) {
                File file = new File(uploadPath + "/" + attachment.getFilePath());
                if (file.exists()) {
                    ZipEntry zipEntry = new ZipEntry(attachment.getFileName());
                    zos.putNextEntry(zipEntry);
                    Files.copy(file.toPath(), zos);
                    zos.closeEntry();
                }
            }
        }
    }
}