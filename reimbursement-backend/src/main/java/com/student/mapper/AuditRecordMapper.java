package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.AuditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
    
    @Select("SELECT * FROM audit_record WHERE detail_id = #{detailId} ORDER BY create_time DESC")
    List<AuditRecord> selectByDetailId(Long detailId);
    
    @Select("SELECT * FROM audit_record WHERE project_id = #{projectId} ORDER BY create_time DESC")
    List<AuditRecord> selectByProjectId(Long projectId);
}
