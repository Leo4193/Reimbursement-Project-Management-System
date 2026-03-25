package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.ProjectDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectDepartmentMapper extends BaseMapper<ProjectDepartment> {
    
    @Select("SELECT department_id FROM project_department WHERE project_id = #{projectId}")
    List<Integer> selectDepartmentIdsByProjectId(Long projectId);
}