package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Project;
import com.student.dto.ProjectDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

public interface ProjectService extends IService<Project> {
    
    Page<Project> queryPage(Integer current, Integer size, String keyword, Integer status, Long userId, String role, Integer userDepartmentId);
    
    boolean addProject(ProjectDto projectDto, Long userId);
    
    boolean updateProject(ProjectDto projectDto);
    
    Map<String, Object> getProjectDetail(Long id);
    
    boolean updateStatus(Long id, Integer status);
    
    String generateProjectNo();
    
    List<Integer> getProjectDepartmentIds(Long projectId);
    
    List<Long> getUserProjectIds(Long userId, String role, Integer userDepartmentId);
}