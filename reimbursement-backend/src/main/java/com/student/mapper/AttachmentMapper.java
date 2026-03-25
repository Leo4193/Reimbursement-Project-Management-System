package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {
    
    @Select("SELECT * FROM attachment WHERE detail_id = #{detailId} ORDER BY upload_time DESC")
    List<Attachment> selectByDetailId(Long detailId);
    
    @Select("SELECT * FROM attachment WHERE project_id = #{projectId}")
    List<Attachment> selectByProjectId(Long projectId);
}