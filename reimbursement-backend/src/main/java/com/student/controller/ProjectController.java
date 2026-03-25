package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.entity.Project;
import com.student.entity.ProjectDepartment;
import com.student.entity.User;
import com.student.dto.ProjectDto;
import com.student.mapper.ProjectDepartmentMapper;
import com.student.mapper.UserMapper;
import com.student.service.ProjectService;
import com.student.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/project")
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectController extends BaseController {

    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ProjectDepartmentMapper projectDepartmentMapper;
    
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/page")
    public Result<Page<Project>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);
        Integer userDepartmentId = null;
        
        System.out.println("=== ProjectController.page 调试 ===");
        System.out.println("userId: " + userId);
        System.out.println("role: " + role);
        
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        // 部门负责人和项目主管都需要获取部门ID
        if ("B".equals(role) || "C".equals(role)) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                userDepartmentId = user.getDepartmentId();
                System.out.println(role.equals("B") ? "部门负责人" : "项目主管" + " - 部门ID: " + userDepartmentId);
            }
        }
        
        Page<Project> page = projectService.queryPage(current, size, keyword, status, userId, role, userDepartmentId);
        return Result.success(page);
    }
    
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        
        Map<String, Object> detail = projectService.getProjectDetail(id);
        if (detail == null) {
            return Result.error("项目不存在");
        }
        return Result.success(detail);
    }

    @PostMapping("/add")
    public Result<String> add(@Valid @RequestBody ProjectDto projectDto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        boolean success = projectService.addProject(projectDto, userId);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody ProjectDto projectDto, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        
        boolean success = projectService.updateProject(projectDto);
        if (success) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);
        
        System.out.println("删除项目 - 用户ID: " + userId + ", 角色: " + role + ", 项目ID: " + id);
        
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        Project project = projectService.getById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        
        // 权限判断（与前端的 canDelete 保持一致）
        if ("A".equals(role)) {
            // 管理员：可以删除所有项目
        } else if ("B".equals(role)) {
            // 部门负责人：只能删除自己部门的项目
            List<Integer> deptIds = projectDepartmentMapper.selectDepartmentIdsByProjectId(id);
            Integer userDeptId = getUserDepartmentId(userId);
            if (userDeptId == null || !deptIds.contains(userDeptId)) {
                return Result.forbidden("权限不足，只能删除本部门的项目");
            }
        } else if ("C".equals(role)) {
            // 项目主管：只能删除自己创建的项目
            if (!project.getCreatorId().equals(userId)) {
                return Result.forbidden("权限不足，只能删除自己创建的项目");
            }
        } else {
            return Result.forbidden("权限不足，无权删除项目");
        }
        
        // 先删除项目-部门关联
        LambdaQueryWrapper<ProjectDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectDepartment::getProjectId, id);
        projectDepartmentMapper.delete(wrapper);
        
        // 再删除项目
        boolean success = projectService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @PutMapping("/status/{id}")
    public Result<String> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        
        boolean success = projectService.updateStatus(id, status);
        if (success) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败");
    }
    
    private Integer getUserDepartmentId(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getDepartmentId() : null;
    }
}