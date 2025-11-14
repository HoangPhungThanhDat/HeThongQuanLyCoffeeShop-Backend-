package com.example.cafe.controllers;

import com.example.cafe.entity.User;
import com.example.cafe.security.services.UserService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestPart("user") User user,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = service.saveImage(imageFile);
                user.setImageUrl(fileName);
            } else {
                user.setImageUrl("default-avatar.png");
            }

            User savedUser = service.save(user);
            return ResponseEntity.ok(savedUser);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }


    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestPart("user") User user,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = service.saveImage(imageFile);
                user.setImageUrl(fileName);
            }

            User updatedUser = service.update(id, user);
            return ResponseEntity.ok(updatedUser);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
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

    // ✅ Lấy ảnh đại diện người dùng
    @GetMapping("/image/{fileName}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable String fileName) {
        try {
            InputStream imageStream = service.getImage(fileName);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentDispositionFormData("inline", fileName);
            return new ResponseEntity<>(new InputStreamResource(imageStream), headers, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
