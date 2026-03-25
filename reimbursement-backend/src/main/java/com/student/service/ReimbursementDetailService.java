package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.ReimbursementDetail;
import com.student.dto.ReimbursementDetailDto;
import com.student.dto.BatchDetailDto;
import java.util.List;
import java.util.Map;

public interface ReimbursementDetailService extends IService<ReimbursementDetail> {
    
    // 批量添加明细
    boolean batchAdd(BatchDetailDto batchDto, Long userId);
    
    // 根据项目ID查询明细列表
    List<ReimbursementDetail> getByProjectId(Long projectId);
    
    // 获取项目明细统计（总金额、数量）
    Map<String, Object> getProjectSummary(Long projectId);
    
    // 删除明细（支持批量）
    boolean batchDelete(List<Long> ids);
    
    // 更新明细状态
    boolean updateStatus(Long id, Integer status);
    
    // 生成明细序号
    String generateDetailNo(Long projectId);
}