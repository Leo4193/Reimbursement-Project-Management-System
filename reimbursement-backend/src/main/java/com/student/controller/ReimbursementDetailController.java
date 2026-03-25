package com.student.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.entity.Project;
import com.student.entity.ReimbursementDetail;
import com.student.entity.User;
import com.student.dto.BatchDetailDto;
import com.student.dto.ReimbursementDetailDto;
import com.student.mapper.ProjectDepartmentMapper;
import com.student.mapper.UserMapper;
import com.student.service.ProjectService;
import com.student.service.ReimbursementDetailService;
import com.student.utils.JwtUtils;
import com.student.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detail")
@CrossOrigin(origins = "http://localhost:3000")
public class ReimbursementDetailController extends BaseController {

    @Autowired
    private ReimbursementDetailService detailService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ProjectDepartmentMapper projectDepartmentMapper;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 批量添加报销明细
     */
    @PostMapping("/batch/add")
    public Result<String> batchAdd(@Valid @RequestBody BatchDetailDto batchDto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录，请重新登录");
        }
        
        if (batchDto.getDetails() == null || batchDto.getDetails().isEmpty()) {
            return Result.error("明细不能为空");
        }
        
        try {
            boolean success = detailService.batchAdd(batchDto, userId);
            if (success) {
                return Result.success("添加成功");
            }
            return Result.error("添加失败，请检查数据完整性");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 根据项目ID查询明细列表
     */
    @GetMapping("/list/{projectId}")
    public Result<List<ReimbursementDetail>> getByProjectId(@PathVariable Long projectId, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        
        List<ReimbursementDetail> list = detailService.getByProjectId(projectId);
        return Result.success(list);
    }

    /**
     * 获取项目明细统计
     */
    @GetMapping("/summary/{projectId}")
    public Result<Map<String, Object>> getProjectSummary(@PathVariable Long projectId, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        
        Map<String, Object> summary = detailService.getProjectSummary(projectId);
        return Result.success(summary);
    }

    /**
     * 查询单个明细
     */
    @GetMapping("/{id}")
    public Result<ReimbursementDetail> getById(@PathVariable Long id, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        
        ReimbursementDetail detail = detailService.getById(id);
        return Result.success(detail);
    }

    /**
     * 修改明细
     */
    @PutMapping("/update")
    public Result<String> update(@Valid @RequestBody ReimbursementDetailDto detailDto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        ReimbursementDetail detail = detailService.getById(detailDto.getId());
        if (detail == null) {
            return Result.error("明细不存在");
        }
        
        // 更新字段
        detail.setDepartment(detailDto.getDepartment());
        detail.setAgent(detailDto.getAgent());
        detail.setStudentNo(detailDto.getStudentNo());
        detail.setContent(detailDto.getContent());
        detail.setUnitPrice(detailDto.getUnitPrice());
        detail.setQuantity(detailDto.getQuantity());
        detail.setTotalAmount(detailDto.getUnitPrice().multiply(new java.math.BigDecimal(detailDto.getQuantity())));
        detail.setInvoiceNo(detailDto.getInvoiceNo());
        detail.setInvoiceType(detailDto.getInvoiceType());
        detail.setReimbursementCategory(detailDto.getReimbursementCategory());
        detail.setAmountCategory(detailDto.getAmountCategory());
        detail.setPaymentMethod(detailDto.getPaymentMethod());
        detail.setRemark(detailDto.getRemark());
        
        // 编辑后重置状态为待审批（0）
        detail.setStatus(0);
        detail.setUpdateTime(LocalDateTime.now());
        
        boolean success = detailService.updateById(detail);
        if (success) {
            return Result.success("修改成功，请重新提交审批");
        }
        return Result.error("修改失败");
    }

    /**
     * 删除单个明细
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String role = getCurrentUserRole(request);
        
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        ReimbursementDetail detail = detailService.getById(id);
        if (detail == null) {
            return Result.error("明细不存在");
        }
        
        Project project = projectService.getById(detail.getProjectId());
        
        // 权限判断
        if ("A".equals(role)) {
            // 管理员：可以删除所有明细
        } else if ("B".equals(role)) {
            // 部门负责人：只能删除自己部门的明细
            java.util.List<Integer> deptIds = projectDepartmentMapper.selectDepartmentIdsByProjectId(detail.getProjectId());
            Integer userDeptId = getUserDepartmentId(userId);
            if (userDeptId == null || !deptIds.contains(userDeptId)) {
                return Result.forbidden("无权删除其他部门的明细");
            }
        } else if ("C".equals(role)) {
            // 项目主管：只能删除自己创建的明细
            if (!project.getCreatorId().equals(userId)) {
                return Result.forbidden("无权删除他人创建的明细");
            }
        } else {
            return Result.forbidden("无权删除");
        }
        
        boolean success = detailService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 批量删除明细
     */
    @DeleteMapping("/batch/delete")
    public Result<String> batchDelete(@RequestBody List<Long> ids, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        boolean success = detailService.batchDelete(ids);
        if (success) {
            return Result.success("批量删除成功");
        }
        return Result.error("批量删除失败");
    }

    /**
     * 更新明细状态（审批用）
     */
    @PutMapping("/status/{id}")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        boolean success = detailService.updateStatus(id, status);
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