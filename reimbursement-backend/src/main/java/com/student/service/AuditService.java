package com.student.service;

import com.student.entity.AuditRecord;
import com.student.dto.AuditDto;
import com.student.dto.BatchAuditDto;
import com.student.dto.ModifyAuditResultDto;
import java.util.List;
import java.util.Map;

public interface AuditService {
    
    // 项目审批
    boolean auditProject(AuditDto auditDto, Long auditorId, String auditorName, String auditorRole);
    
    // 明细审批
    boolean auditDetail(AuditDto auditDto, Long auditorId, String auditorName, String auditorRole);
    
    // 批量审批明细
    Map<String, Object> batchAuditDetail(BatchAuditDto batchAuditDto, Long auditorId, String auditorName, String auditorRole);
    
    // 修改审批结果
    boolean modifyAuditResult(ModifyAuditResultDto modifyDto, Long auditorId, String auditorName, String auditorRole);
    
    // 查询明细审批历史
    List<AuditRecord> getDetailAuditHistory(Long detailId);
    
    // 查询项目审批历史
    List<AuditRecord> getProjectAuditHistory(Long projectId);
    
    // 获取明细当前状态
    Integer getDetailCurrentStatus(Long detailId);
}