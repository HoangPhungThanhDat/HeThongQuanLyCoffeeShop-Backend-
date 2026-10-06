package com.example.cafe.controllers;

import com.example.cafe.entity.User;
import com.example.cafe.security.services.UserService;
import com.example.cafe.services.CloudinaryService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;
    private final CloudinaryService cloudinaryService;

    public UserController(UserService service, CloudinaryService cloudinaryService) {
        this.service = service;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getOne(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Thêm user — upload ảnh lên Cloudinary
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestPart("user") User user,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                user.setImageUrl(imageUrl);
            } else {
                user.setImageUrl("default-avatar.png");
            }

            User savedUser = service.save(user);
            return ResponseEntity.ok(savedUser);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Upload ảnh thất bại: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ✅ Cập nhật user — nếu có ảnh mới thì upload lên Cloudinary
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestPart("user") User user,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(imageFile);
                user.setImageUrl(imageUrl);
            }

            User updatedUser = service.update(id, user);
            return ResponseEntity.ok(updatedUser);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Upload ảnh thất bại: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ✅ Upload ảnh riêng — trả về URL Cloudinary
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadFile(file);
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}