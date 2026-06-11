package com.ruoyi.web.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RestController
@RequestMapping("/ai")
public class AIController {

    @PostMapping("/classify-bone")
    public String classifyBone(@RequestParam("image") MultipartFile image) {
        try {
            // 1. 准备请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });

            // 2. 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 3. 调用 Python 服务
            RestTemplate rest = new RestTemplate();
            String pythonServiceUrl = "http://localhost:8001/predict";
            ResponseEntity<String> response = rest.postForEntity(pythonServiceUrl, requestEntity, String.class);

            // 4. 返回结果给前端
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"调用AI服务失败：" + e.getMessage() + "\"}";
        }
    }
}