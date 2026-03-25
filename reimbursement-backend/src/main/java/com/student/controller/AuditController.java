package com.student.controller;

import com.student.entity.AuditRecord;
import com.student.dto.AuditDto;
import com.student.dto.BatchAuditDto;
import com.student.dto.ModifyAuditResultDto;
import com.student.service.AuditService;
import com.student.service.ProjectService;
import com.student.service.ReimbursementDetailService;
import com.student.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "http://localhost:3000")
public class AuditController extends BaseController {

    @Autowired
    private AuditService auditService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ReimbursementDetailService detailService;

    /**
     * 项目审批
     */
    @PostMapping("/project")
    public Result<String> auditProject(@Valid @RequestBody AuditDto auditDto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String username = getCurrentUsername(request);
        String role = getCurrentUserRole(request);
        
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        // 权限检查：只有管理员和部门负责人可以审批项目
        if (!"A".equals(role) && !"B".equals(role)) {
            return Result.forbidden("无权进行项目审批");
        }
        
        boolean success = auditService.auditProject(auditDto, userId, username, role);
        if (success) {
            return Result.success("项目审批成功");
        }
        return Result.error("项目审批失败");
    }

    /**
     * 明细审批
     */
    @PostMapping("/detail")
    public Result<String> auditDetail(@Valid @RequestBody AuditDto auditDto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String username = getCurrentUsername(request);
        String role = getCurrentUserRole(request);
        
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        // 只有管理员和部门负责人可以审批明细
        if (!"A".equals(role) && !"B".equals(role)) {
            return Result.forbidden("权限不足，只有管理员和部门负责人可以审批");
        }
        
        try {
            boolean success = auditService.auditDetail(auditDto, userId, username, role);
            if (success) {
                return Result.success("明细审批成功");
            }
            return Result.error("明细审批失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审批失败：" + e.getMessage());
        }
    }

    /**
     * 批量审批明细
     */
    @PostMapping("/detail/batch")
    public Result<Map<String, Object>> batchAuditDetail(@Valid @RequestBody BatchAuditDto batchAuditDto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String username = getCurrentUsername(request);
        String role = getCurrentUserRole(request);
        
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        // 权限检查：只有管理员和部门负责人可以批量审批
        if (!"A".equals(role) && !"B".equals(role)) {
            return Result.forbidden("无权进行批量审批");
        }
        
        Map<String, Object> result = auditService.batchAuditDetail(batchAuditDto, userId, username, role);
        return Result.success(result);
    }

    /**
     * 修改审批结果
     */
    @PostMapping("/modify")
    public Result<String> modifyAuditResult(@Valid @RequestBody ModifyAuditResultDto modifyDto, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String username = getCurrentUsername(request);
        String role = getCurrentUserRole(request);
        
        if (userId == null) {
            return Result.unauthorized("未登录");
        }
        
        // 只有管理员可以修改审批结果（部门负责人也不能修改）
        if (!"A".equals(role)) {
            return Result.forbidden("权限不足，只有管理员可以修改审批结果");
        }
        
        try {
            boolean success = auditService.modifyAuditResult(modifyDto, userId, username, role);
            if (success) {
                return Result.success("修改审批结果成功");
            }
            return Result.error("修改审批结果失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    /**
     * 查询明细审批历史
     */
    @GetMapping("/history/detail/{detailId}")
    public Result<List<AuditRecord>> getDetailAuditHistory(@PathVariable Long detailId, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        
        List<AuditRecord> history = auditService.getDetailAuditHistory(detailId);
        return Result.success(history);
    }

    /**
     * 查询项目审批历史
     */
    @GetMapping("/history/project/{projectId}")
    public Result<List<AuditRecord>> getProjectAuditHistory(@PathVariable Long projectId, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        
        List<AuditRecord> history = auditService.getProjectAuditHistory(projectId);
        return Result.success(history);
    }

    /**
     * 获取明细当前状态
     */
    @GetMapping("/status/detail/{detailId}")
    public Result<Integer> getDetailStatus(@PathVariable Long detailId, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        
        Integer status = auditService.getDetailCurrentStatus(detailId);
        return Result.success(status);
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        
        // 生成6位随机验证码
        String code = String.format("%06d", new java.util.Random().nextInt(999999));
        
        // 存储到缓存（这里简化处理，实际应该用Redis）
        // 实际项目中可以用Redis，设置5分钟过期
        // 这里简单用Map存储，生产环境请用Redis
        System.out.println("验证码: " + code + " 发送到: " + email);
        
        // TODO: 发送邮件
        // 这里需要配置邮件服务，暂时打印日志
        
        return Result.success("验证码已发送");
    }
}