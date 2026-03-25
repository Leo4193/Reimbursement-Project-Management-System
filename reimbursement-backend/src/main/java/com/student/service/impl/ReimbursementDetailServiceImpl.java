package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.ReimbursementDetail;
import com.student.dto.ReimbursementDetailDto;
import com.student.dto.BatchDetailDto;
import com.student.mapper.ReimbursementDetailMapper;
import com.student.service.ReimbursementDetailService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReimbursementDetailServiceImpl extends ServiceImpl<ReimbursementDetailMapper, ReimbursementDetail> implements ReimbursementDetailService {

    @Override
    @Transactional
    public boolean batchAdd(BatchDetailDto batchDto, Long userId) {
        List<ReimbursementDetailDto> detailDtos = batchDto.getDetails();
        if (detailDtos == null || detailDtos.isEmpty()) {
            throw new RuntimeException("明细不能为空");
        }
        
        // 数据校验
        for (ReimbursementDetailDto dto : detailDtos) {
            if (dto.getDepartment() == null || dto.getDepartment().trim().isEmpty()) {
                throw new RuntimeException("部门不能为空");
            }
            if (dto.getAgent() == null || dto.getAgent().trim().isEmpty()) {
                throw new RuntimeException("经办人不能为空");
            }
            if (dto.getStudentNo() == null || dto.getStudentNo().trim().isEmpty()) {
                throw new RuntimeException("学号不能为空");
            }
            if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                throw new RuntimeException("内容不能为空");
            }
            if (dto.getUnitPrice() == null || dto.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("单价必须大于0");
            }
            if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                throw new RuntimeException("数量必须大于0");
            }
            if (dto.getInvoiceNo() == null || dto.getInvoiceNo().trim().isEmpty()) {
                throw new RuntimeException("发票号码不能为空");
            }
        }
        
        Long projectId = batchDto.getProjectId();
        
        for (ReimbursementDetailDto dto : detailDtos) {
            ReimbursementDetail detail = new ReimbursementDetail();
            BeanUtils.copyProperties(dto, detail);
            
            detail.setProjectId(projectId);
            detail.setCreatorId(userId);
            
            BigDecimal total = dto.getUnitPrice().multiply(new BigDecimal(dto.getQuantity()));
            detail.setTotalAmount(total);
            detail.setDetailNo(generateDetailNo(projectId));
            detail.setStatus(0);
            detail.setCreateTime(LocalDateTime.now());
            
            // 使用单条插入，避免批量插入的自增ID问题
            this.save(detail);
        }
        
        return true;
    }

    @Override
    public List<ReimbursementDetail> getByProjectId(Long projectId) {
        LambdaQueryWrapper<ReimbursementDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReimbursementDetail::getProjectId, projectId)
               .orderByAsc(ReimbursementDetail::getId);
        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> getProjectSummary(Long projectId) {
        List<ReimbursementDetail> details = getByProjectId(projectId);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = details.size();
        int approvedCount = 0;
        int pendingCount = 0;
        int rejectedCount = 0;
        
        for (ReimbursementDetail detail : details) {
            totalAmount = totalAmount.add(detail.getTotalAmount());
            
            switch (detail.getStatus()) {
                case 0: pendingCount++; break;
                case 1: approvedCount++; break;
                case 2: rejectedCount++; break;
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAmount", totalAmount);
        summary.put("totalCount", totalCount);
        summary.put("approvedCount", approvedCount);
        summary.put("pendingCount", pendingCount);
        summary.put("rejectedCount", rejectedCount);
        
        return summary;
    }

    @Override
    @Transactional
    public boolean batchDelete(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        ReimbursementDetail detail = new ReimbursementDetail();
        detail.setId(id);
        detail.setStatus(status);
        return this.updateById(detail);
    }

    @Override
    public String generateDetailNo(Long projectId) {
        String maxNo = baseMapper.getMaxDetailNo(projectId);
        if (maxNo == null || "D000".equals(maxNo)) {
            return "D001";
        }
        
        // 取出后三位序号
        String lastThree = maxNo.substring(1);
        int num = Integer.parseInt(lastThree) + 1;
        return "D" + String.format("%03d", num);
    }
}