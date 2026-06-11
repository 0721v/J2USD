package com.giftcard.controller;

import com.giftcard.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String UPLOAD_DIR = "./uploads/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            HttpServletRequest request) {

        Map<String, Object> result = new HashMap<>();

        // 验证登录
        String jwtToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.substring(7);
        } else if (token != null && !token.isEmpty()) {
            jwtToken = token;
        }

        if (jwtToken == null) {
            result.put("code", 401);
            result.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(result);
        }

        try {
            jwtUtil.verifyToken(jwtToken);
        } catch (Exception e) {
            result.put("code", 401);
            result.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(result);
        }

        // 验证文件
        if (file.isEmpty()) {
            result.put("code", 400);
            result.put("message", "请选择要上传的文件");
            return ResponseEntity.badRequest().body(result);
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            result.put("code", 400);
            result.put("message", "文件大小不能超过5MB");
            return ResponseEntity.badRequest().body(result);
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidImageType(originalFilename)) {
            result.put("code", 400);
            result.put("message", "只支持上传 JPG、PNG、GIF、WebP 格式的图片");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成文件名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(newFilename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath);

            // 构建访问URL
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPortNum = request.getServerPort();
            
            String fileUrl;
            if ((scheme.equals("http") && serverPortNum == 80) || (scheme.equals("https") && serverPortNum == 443)) {
                fileUrl = scheme + "://" + serverName + "/uploads/" + newFilename;
            } else {
                fileUrl = scheme + "://" + serverName + ":" + serverPortNum + "/uploads/" + newFilename;
            }

            result.put("code", 200);
            result.put("message", "上传成功");
            result.put("data", Map.of(
                    "url", fileUrl,
                    "filename", newFilename,
                    "size", file.getSize()
            ));

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    private boolean isValidImageType(String filename) {
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".jpg") || 
               lowerFilename.endsWith(".jpeg") || 
               lowerFilename.endsWith(".png") || 
               lowerFilename.endsWith(".gif") ||
               lowerFilename.endsWith(".webp");
    }
}
