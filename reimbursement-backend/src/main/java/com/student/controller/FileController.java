package com.student.controller;

import com.student.entity.Attachment;
import com.student.mapper.AttachmentMapper;
import com.student.utils.JwtUtils;
import com.student.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "http://localhost:3000")
public class FileController extends BaseController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Autowired
    private AttachmentMapper attachmentMapper;
    
    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<Attachment> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "detailId", required = false) Long detailId,
            HttpServletRequest request) {
        
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }

        try {
            // 检查文件大小（10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.error("文件大小不能超过10MB");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            
            if (!(contentType != null && (contentType.startsWith("image/") || contentType.equals("application/pdf"))) 
                && !(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("pdf"))) {
                return Result.error("只支持JPG、PNG、PDF格式");
            }

            // 创建上传目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String newFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            
            // 按日期分目录
            String datePath = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            File targetDir = new File(uploadPath + "/" + datePath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            
            // 保存文件
            String filePath = datePath + "/" + newFilename;
            File targetFile = new File(uploadPath + "/" + filePath);
            file.transferTo(targetFile);

            // 保存附件记录
            Attachment attachment = new Attachment();
            attachment.setProjectId(projectId);
            attachment.setDetailId(detailId);
            attachment.setFileName(originalFilename);
            attachment.setFilePath(filePath);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(contentType);
            attachment.setUploadBy(userId);
            attachment.setUploadTime(LocalDateTime.now());
            
            attachmentMapper.insert(attachment);
            
            return Result.success(attachment);
            
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, 
                        @RequestParam(required = false) String preview,
                        @RequestParam(required = false) String token,
                        HttpServletResponse response, 
                        HttpServletRequest request) {
        try {
            // 优先从请求头获取token
            String authToken = request.getHeader("Authorization");
            String validToken = null;
            
            if (authToken != null && authToken.startsWith("Bearer ")) {
                validToken = authToken.substring(7);
            } else if (token != null && !token.isEmpty()) {
                // 从URL参数获取token
                validToken = token;
            }
            
            if (validToken == null) {
                response.setStatus(401);
                response.getWriter().write("未登录，请重新登录");
                return;
            }
            
            // 验证token
            if (!jwtUtils.validateToken(validToken)) {
                response.setStatus(401);
                response.getWriter().write("登录已过期，请重新登录");
                return;
            }
            
            Attachment attachment = attachmentMapper.selectById(id);
            if (attachment == null) {
                response.setStatus(404);
                response.getWriter().write("文件不存在");
                return;
            }

            File file = new File(uploadPath + "/" + attachment.getFilePath());
            if (!file.exists()) {
                response.setStatus(404);
                response.getWriter().write("文件不存在");
                return;
            }

            // 设置响应头
            String encodedFileName = URLEncoder.encode(attachment.getFileName(), "UTF-8");
            if (preview != null && preview.equals("true")) {
                response.setHeader("Content-Disposition", "inline; filename=\"" + encodedFileName + "\"");
            } else {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            }
            
            response.setContentType(attachment.getFileType());
            response.setContentLengthLong(attachment.getFileSize());

            try (InputStream is = new FileInputStream(file);
                OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setStatus(500);
                response.getWriter().write("下载失败：" + e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    /**
     * 根据明细ID查询附件列表
     */
    @GetMapping("/list/{detailId}")
    public Result<List<Attachment>> getByDetailId(@PathVariable Long detailId, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        List<Attachment> list = attachmentMapper.selectByDetailId(detailId);
        return Result.success(list);
    }

    /**
     * 根据项目ID查询附件列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<Attachment>> getByProjectId(@PathVariable Long projectId, HttpServletRequest request) {
        if (getCurrentUserId(request) == null) {
            return Result.unauthorized("未登录");
        }
        List<Attachment> list = attachmentMapper.selectByProjectId(projectId);
        return Result.success(list);
    }

    /**
     * 删除附件
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.unauthorized("未登录");
        }

        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            return Result.error("文件不存在");
        }

        // 获取当前用户角色
        String role = getCurrentUserRole(request);
        
        // 检查权限（只能删除自己上传的，或者管理员可以删除所有）
        if (!attachment.getUploadBy().equals(userId) && !"A".equals(role)) {
            return Result.forbidden("无权删除此文件");
        }

        // 删除物理文件
        File file = new File(uploadPath + "/" + attachment.getFilePath());
        if (file.exists()) {
            file.delete();
        }

        // 删除数据库记录
        attachmentMapper.deleteById(id);
        
        return Result.success("删除成功");
    }
}