package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.Project;
import com.student.entity.ProjectDepartment;
import com.student.entity.SysDict;
import com.student.dto.ProjectDto;
import com.student.mapper.ProjectDepartmentMapper;
import com.student.mapper.ProjectMapper;
import com.student.mapper.SysDictMapper;
import com.student.service.ProjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Autowired
    private SysDictMapper sysDictMapper;
    
    @Autowired
    private ProjectDepartmentMapper projectDepartmentMapper;

    @Override
    public Page<Project> queryPage(Integer current, Integer size, String keyword, Integer status, 
                                    Long userId, String role, Integer userDepartmentId) {
        System.out.println("=== queryPage 调试 ===");
        System.out.println("userId: " + userId);
        System.out.println("role: " + role);
        System.out.println("userDepartmentId: " + userDepartmentId);
        
        Page<Project> page = new Page<>(current, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(Project::getName, keyword)
                .or()
                .like(Project::getProjectLeader, keyword)
                .or()
                .like(Project::getProjectNo, keyword)
            );
        }
        
        // 状态筛选
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }
        
        // 数据权限控制
        if ("A".equals(role)) {
            // 管理员：无限制
        } else if ("B".equals(role) && userDepartmentId != null) {
            // 部门负责人：能看到本部门相关的项目
            wrapper.inSql(Project::getId, 
                "SELECT project_id FROM project_department WHERE department_id = " + userDepartmentId);
        } else if ("C".equals(role) && userDepartmentId != null) {
            // 项目主管：能看到自己所在部门相关的项目（和部门负责人一样）
            wrapper.inSql(Project::getId, 
                "SELECT project_id FROM project_department WHERE department_id = " + userDepartmentId);
        } else {
            wrapper.eq(Project::getId, -1L);
        }
        
        wrapper.orderByDesc(Project::getCreateTime);
        
        Page<Project> result = this.page(page, wrapper);
        System.out.println("查询结果数量: " + result.getRecords().size());
        
        return result;
    }

    @Override
    @Transactional
    public boolean addProject(ProjectDto projectDto, Long userId) {
        System.out.println("=== 创建项目 ===");
        System.out.println("userId: " + userId);
        
        Project project = new Project();
        BeanUtils.copyProperties(projectDto, project);
        
        project.setProjectNo(generateProjectNo());
        project.setCreatorId(userId);
        project.setApplyTime(LocalDateTime.now());
        project.setStatus(0);
        
        project.setDepartmentFinanceLeader(projectDto.getDepartmentFinanceLeader());
        project.setOfficeFinanceLeader(projectDto.getOfficeFinanceLeader());
        project.setActivityChairperson(projectDto.getActivityChairperson());
        
        this.save(project);
        System.out.println("创建项目成功 - ID: " + project.getId() + ", creatorId: " + project.getCreatorId());
        
        // 保存项目-部门关联
        if (projectDto.getDepartmentIds() != null && !projectDto.getDepartmentIds().isEmpty()) {
            for (Integer deptId : projectDto.getDepartmentIds()) {
                ProjectDepartment pd = new ProjectDepartment();
                pd.setProjectId(project.getId());
                pd.setDepartmentId(deptId);
                projectDepartmentMapper.insert(pd);
            }
        }
        
        return true;
    }

    @Override
    @Transactional
    public boolean updateProject(ProjectDto projectDto) {
        if (projectDto.getId() == null) {
            throw new RuntimeException("项目ID不能为空");
        }
        
        Project project = new Project();
        BeanUtils.copyProperties(projectDto, project);
        this.updateById(project);
        
        // 更新项目-部门关联：先删除旧的，再插入新的
        LambdaQueryWrapper<ProjectDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectDepartment::getProjectId, projectDto.getId());
        projectDepartmentMapper.delete(wrapper);
        
        if (projectDto.getDepartmentIds() != null && !projectDto.getDepartmentIds().isEmpty()) {
            for (Integer deptId : projectDto.getDepartmentIds()) {
                ProjectDepartment pd = new ProjectDepartment();
                pd.setProjectId(projectDto.getId());
                pd.setDepartmentId(deptId);
                projectDepartmentMapper.insert(pd);
            }
        }
        
        return true;
    }

    @Override
    public Map<String, Object> getProjectDetail(Long id) {
        Project project = this.getById(id);
        if (project == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("project", project);
        
        // 获取关联的部门ID列表
        List<Integer> departmentIds = getProjectDepartmentIds(id);
        result.put("departmentIds", departmentIds);
        
        // 获取部门名称列表（使用全称）
        List<String> departmentNames = new ArrayList<>();
        for (Integer deptId : departmentIds) {
            String deptName = getDepartmentFullName(deptId);
            departmentNames.add(deptName);
        }
        result.put("departmentNames", departmentNames);
        
        // 转换类型名称
        if (project.getTypeId() != null) {
            String typeName = getActivityTypeName(project.getTypeId());
            result.put("typeName", typeName);
        }
        
        result.put("statusText", getStatusText(project.getStatus()));
        
        return result;
    }

    // 添加获取部门全称的方法
    private String getDepartmentFullName(Integer deptId) {
        Map<Integer, String> deptMap = new HashMap<>();
        deptMap.put(1, "文案会务与人事管理工作项目组");
        deptMap.put(2, "学风建设与文化促进工作项目组");
        deptMap.put(3, "学生权益与提案调研工作项目组");
        deptMap.put(4, "外联公关与社会融入工作项目组");
        deptMap.put(5, "品牌建设与媒体推广工作项目组");
        deptMap.put(6, "活动创意与素质拓展工作项目组");
        return deptMap.getOrDefault(deptId, "未知部门");
    }

    // 添加获取活动类型名称的方法
    private String getActivityTypeName(Integer typeId) {
        Map<Integer, String> typeMap = new HashMap<>();
        typeMap.put(1, "对外活动");
        typeMap.put(2, "校级大型活动");
        typeMap.put(3, "会内活动");
        typeMap.put(4, "其他活动");
        return typeMap.getOrDefault(typeId, "未知类型");
    }

    // 确保 getStatusText 方法存在
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "草稿";
            case 1: return "待审批";
            case 2: return "进行中";
            case 3: return "已完成";
            case 4: return "已驳回";
            default: return "未知";
        }
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        Project project = new Project();
        project.setId(id);
        project.setStatus(status);
        return this.updateById(project);
    }

    @Override
    public String generateProjectNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String maxNo = baseMapper.getMaxProjectNo();
        
        if (maxNo == null || "P000000".equals(maxNo)) {
            return "P" + dateStr + "001";
        }
        
        String lastThree = maxNo.substring(maxNo.length() - 3);
        int num = Integer.parseInt(lastThree) + 1;
        String seq = String.format("%03d", num);
        
        return "P" + dateStr + seq;
    }
    
    @Override
    public List<Integer> getProjectDepartmentIds(Long projectId) {
        return projectDepartmentMapper.selectDepartmentIdsByProjectId(projectId);
    }
    
    @Override
    public List<Long> getUserProjectIds(Long userId, String role, Integer userDepartmentId) {
        if ("A".equals(role)) {
            // 管理员可以查看所有
            return null;
        } else if ("B".equals(role) && userDepartmentId != null) {
            // 部门负责人：查看本部门相关的项目
            List<ProjectDepartment> list = projectDepartmentMapper.selectList(null);
            return list.stream()
                .filter(pd -> pd.getDepartmentId().equals(userDepartmentId))
                .map(ProjectDepartment::getProjectId)
                .distinct()
                .collect(Collectors.toList());
        } else if ("C".equals(role)) {
            // 项目主管：查看自己创建的项目
            LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Project::getCreatorId, userId);
            return this.list(wrapper).stream().map(Project::getId).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

}