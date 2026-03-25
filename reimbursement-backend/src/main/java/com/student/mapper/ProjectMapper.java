package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.entity.Project;
import com.student.annotation.DataScope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    
    // 生成项目编号：P + 年月日 + 序号
    @Select("SELECT ISNULL(MAX(project_no), 'P000000') FROM project WHERE project_no LIKE 'P' + CONVERT(VARCHAR, GETDATE(), 112) + '%'")
    String getMaxProjectNo();
    
    // 查询所有项目（带数据权限）
    @Select("SELECT * FROM project")
    @DataScope(deptAlias = "department_id", userAlias = "creator_id")
    List<Project> selectAllWithDataScope();
}