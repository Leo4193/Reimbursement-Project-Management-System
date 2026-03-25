package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.AuditRecord;
import com.student.entity.Project;
import com.student.entity.ReimbursementDetail;
import com.student.dto.AuditDto;
import com.student.dto.BatchAuditDto;
import com.student.dto.ModifyAuditResultDto;
import com.student.mapper.AuditRecordMapper;
import com.student.mapper.ProjectMapper;
import com.student.mapper.ReimbursementDetailMapper;
import com.student.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord> implements AuditService {

    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ReimbursementDetailMapper detailMapper;
    
    @Autowired
    private AuditRecordMapper auditRecordMapper;

    @Override
    @Transactional
    public boolean auditProject(AuditDto auditDto, Long auditorId, String auditorName, String auditorRole) {
        Long projectId = auditDto.getTargetId();
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        Integer beforeStatus = project.getStatus();
        Integer afterStatus = auditDto.getResult() == 1 ? 2 : 4; // 通过→进行中(2)，驳回→已驳回(4)
        
        // 更新项目状态
        project.setStatus(afterStatus);
        projectMapper.updateById(project);
        
        // 记录审批历史
        AuditRecord record = new AuditRecord();
        record.setProjectId(projectId);
        record.setAuditorId(auditorId);
        record.setAuditorName(auditorName);
        record.setAuditorRole(auditorRole);
        record.setOperationType("PROJECT_AUDIT");
        record.setBeforeStatus(beforeStatus);
        record.setAfterStatus(afterStatus);
        record.setComment(auditDto.getComment());
        record.setCreateTime(LocalDateTime.now());
        
        return auditRecordMapper.insert(record) > 0;
    }

    @Override
    @Transactional
    public boolean auditDetail(AuditDto auditDto, Long auditorId, String auditorName, String auditorRole) {
        Long detailId = auditDto.getTargetId();
        ReimbursementDetail detail = detailMapper.selectById(detailId);
        if (detail == null) {
            throw new RuntimeException("报销明细不存在");
        }
        
        // 驳回时必须填写意见
        if (auditDto.getResult() == 2 && (auditDto.getComment() == null || auditDto.getComment().trim().isEmpty())) {
            throw new RuntimeException("驳回时必须填写审批意见");
        }
        
        Integer beforeStatus = detail.getStatus();
        Integer afterStatus = auditDto.getResult(); // 1:通过 2:驳回
        
        // 更新明细状态
        detail.setStatus(afterStatus);
        detailMapper.updateById(detail);
        
        // 记录审批历史
        AuditRecord record = new AuditRecord();
        record.setDetailId(detailId);
        record.setProjectId(detail.getProjectId());
        record.setAuditorId(auditorId);
        record.setAuditorName(auditorName);
        record.setAuditorRole(auditorRole);
        record.setOperationType("DETAIL_AUDIT");
        record.setBeforeStatus(beforeStatus);
        record.setAfterStatus(afterStatus);
        record.setComment(auditDto.getComment());
        record.setCreateTime(LocalDateTime.now());
        
        return auditRecordMapper.insert(record) > 0;
    }

    @Override
    @Transactional
    public Map<String, Object> batchAuditDetail(BatchAuditDto batchAuditDto, Long auditorId, String auditorName, String auditorRole) {
        List<Long> targetIds = batchAuditDto.getTargetIds();
        Integer result = batchAuditDto.getResult();
        String comment = batchAuditDto.getComment();
        
        // 驳回时必须填写意见
        if (result == 2 && (comment == null || comment.trim().isEmpty())) {
            throw new RuntimeException("批量驳回时必须填写审批意见");
        }
        
        int successCount = 0;
        int failCount = 0;
        StringBuilder failMessages = new StringBuilder();
        
        for (Long detailId : targetIds) {
            try {
                ReimbursementDetail detail = detailMapper.selectById(detailId);
                if (detail == null) {
                    failCount++;
                    failMessages.append("明细ID ").append(detailId).append(" 不存在; ");
                    continue;
                }
                
                // 只能审批待审批状态
                if (detail.getStatus() != 0) {
                    failCount++;
                    failMessages.append("明细ID ").append(detailId).append(" 状态不是待审批; ");
                    continue;
                }
                
                Integer beforeStatus = detail.getStatus();
                Integer afterStatus = result;
                
                // 更新状态
                detail.setStatus(afterStatus);
                detailMapper.updateById(detail);
                
                // 记录审批历史
                AuditRecord record = new AuditRecord();
                record.setDetailId(detailId);
                record.setProjectId(detail.getProjectId());
                record.setAuditorId(auditorId);
                record.setAuditorName(auditorName);
                record.setAuditorRole(auditorRole);
                record.setOperationType("DETAIL_AUDIT");
                record.setBeforeStatus(beforeStatus);
                record.setAfterStatus(afterStatus);
                record.setComment(comment);
                record.setCreateTime(LocalDateTime.now());
                
                auditRecordMapper.insert(record);
                successCount++;
                
            } catch (Exception e) {
                failCount++;
                failMessages.append("明细ID ").append(detailId).append(": ").append(e.getMessage()).append("; ");
            }
        }
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("successCount", successCount);
        resultMap.put("failCount", failCount);
        resultMap.put("failMessages", failMessages.toString());
        
        return resultMap;
    }

    @Override
    @Transactional
    public boolean modifyAuditResult(ModifyAuditResultDto modifyDto, Long auditorId, String auditorName, String auditorRole) {
        Long detailId = modifyDto.getDetailId();
        Integer newStatus = modifyDto.getNewStatus();
        String comment = modifyDto.getComment();
        
        ReimbursementDetail detail = detailMapper.selectById(detailId);
        if (detail == null) {
            throw new RuntimeException("报销明细不存在");
        }
        
        Integer beforeStatus = detail.getStatus();
        
        // 更新状态
        detail.setStatus(newStatus);
        detailMapper.updateById(detail);
        
        // 记录修改历史
        AuditRecord record = new AuditRecord();
        record.setDetailId(detailId);
        record.setProjectId(detail.getProjectId());
        record.setAuditorId(auditorId);
        record.setAuditorName(auditorName);
        record.setAuditorRole(auditorRole);
        record.setOperationType("MODIFY_RESULT");
        record.setBeforeStatus(beforeStatus);
        record.setAfterStatus(newStatus);
        record.setComment(comment);
        record.setCreateTime(LocalDateTime.now());
        
        return auditRecordMapper.insert(record) > 0;
    }

    @Override
    public List<AuditRecord> getDetailAuditHistory(Long detailId) {
        return auditRecordMapper.selectByDetailId(detailId);
    }

    @Override
    public List<AuditRecord> getProjectAuditHistory(Long projectId) {
        return auditRecordMapper.selectByProjectId(projectId);
    }

    @Override
    public Integer getDetailCurrentStatus(Long detailId) {
        ReimbursementDetail detail = detailMapper.selectById(detailId);
        return detail != null ? detail.getStatus() : null;
    }
}