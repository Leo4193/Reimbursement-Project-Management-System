package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.ReimbursementDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ReimbursementDetailMapper extends BaseMapper<ReimbursementDetail> {
    
    // 根据项目ID查询明细
    @Select("SELECT * FROM reimbursement_detail WHERE project_id = #{projectId} ORDER BY id")
    List<ReimbursementDetail> selectByProjectId(Long projectId);
    
    // 生成明细序号
    @Select("SELECT ISNULL(MAX(detail_no), 'D000') FROM reimbursement_detail WHERE project_id = #{projectId}")
    String getMaxDetailNo(Long projectId);
}