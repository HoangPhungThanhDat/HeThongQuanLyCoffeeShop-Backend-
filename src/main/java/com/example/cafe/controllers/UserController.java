// package com.example.cafe.controllers;

// import com.example.cafe.entity.User;
// import com.example.cafe.security.services.UserService;
// import org.springframework.core.io.InputStreamResource;
// import org.springframework.http.*;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.FileNotFoundException;
// import java.io.InputStream;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/users")
// public class UserController {
//     private final UserService service;

//     public UserController(UserService service) {
//         this.service = service;
//     }

//     @GetMapping
//     public ResponseEntity<List<User>> getAll() {
//         return ResponseEntity.ok(service.findAll());
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<User> getOne(@PathVariable Long id) {
//         return service.findById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//     public ResponseEntity<?> create(
//             @RequestPart("user") User user,
//             @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
//         try {
//             if (imageFile != null && !imageFile.isEmpty()) {
//                 String fileName = service.saveImage(imageFile);
//                 user.setImageUrl(fileName);
//             } else {
//                 user.setImageUrl("default-avatar.png");
//             }

//             User savedUser = service.save(user);
//             return ResponseEntity.ok(savedUser);
            
//         } catch (RuntimeException e) {
//             Map<String, String> error = new HashMap<>();
//             error.put("message", e.getMessage());
//             return ResponseEntity.badRequest().body(error);
//         }
//     }


//     @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//     public ResponseEntity<?> update(
//             @PathVariable Long id,
//             @RequestPart("user") User user,
//             @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
//         try {
//             if (imageFile != null && !imageFile.isEmpty()) {
//                 String fileName = service.saveImage(imageFile);
//                 user.setImageUrl(fileName);
//             }

//             User updatedUser = service.update(id, user);
//             return ResponseEntity.ok(updatedUser);
            
//         } catch (RuntimeException e) {
//             Map<String, String> error = new HashMap<>();
//             error.put("message", e.getMessage());
//             return ResponseEntity.badRequest().body(error);
//         }
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<?> delete(@PathVariable Long id) {
//         try {
//             service.delete(id);
//             return ResponseEntity.noContent().build();
//         } catch (RuntimeException e) {
// Map<String, String> error = new HashMap<>();
//             error.put("message", e.getMessage());
//             return ResponseEntity.badRequest().body(error);
//         }
//     }

//     // ✅ Lấy ảnh đại diện người dùng
//     @GetMapping("/image/{fileName}")
//     public ResponseEntity<InputStreamResource> getImage(@PathVariable String fileName) {
//         try {
//             InputStream imageStream = service.getImage(fileName);
//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(MediaType.IMAGE_JPEG);
//             headers.setContentDispositionFormData("inline", fileName);
//             return new ResponseEntity<>(new InputStreamResource(imageStream), headers, HttpStatus.OK);
//         } catch (FileNotFoundException e) {
//             return ResponseEntity.notFound().build();
//         }
//     }
// }






























package com.example.cafe.controllers;

import com.example.cafe.entity.User;
import com.example.cafe.security.services.UserService;
import com.example.cafe.services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    private final CloudinaryService cloudinaryService;

    @Autowired
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

    // ✅ Thêm user với Cloudinary
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestPart("user") User user,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                // ✅ Upload lên Cloudinary
                String imageUrl = cloudinaryService.uploadImage(imageFile, "users");
                user.setImageUrl(imageUrl);
            } else {
                // ✅ Set null thay vì default-avatar.png
                user.setImageUrl(null);
            }

            User savedUser = service.save(user);
            return ResponseEntity.ok(savedUser);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error creating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Cập nhật user với Cloudinary
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestPart("user") User user,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        
        try {
            // Lấy user hiện tại
            User existingUser = service.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (imageFile != null && !imageFile.isEmpty()) {
                // ✅ Xóa ảnh cũ trên Cloudinary (nếu có)
                if (existingUser.getImageUrl() != null && !existingUser.getImageUrl().isEmpty()) {
                    try {
                        cloudinaryService.deleteImage(existingUser.getImageUrl());
                    } catch (Exception e) {
                        // Log warning nhưng không block update
                        System.err.println("Warning: Could not delete old image: " + e.getMessage());
                    }
                }

                // ✅ Upload ảnh mới lên Cloudinary
                String newImageUrl = cloudinaryService.uploadImage(imageFile, "users");
                user.setImageUrl(newImageUrl);
            } else {
                // Giữ nguyên ảnh cũ nếu không upload ảnh mới
                user.setImageUrl(existingUser.getImageUrl());
            }

            User updatedUser = service.update(id, user);
            return ResponseEntity.ok(updatedUser);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error updating user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ✅ Xóa user và ảnh trên Cloudinary
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            // Lấy user để lấy URL ảnh
            User user = service.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Xóa ảnh trên Cloudinary (nếu có)
            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                try {
                    cloudinaryService.deleteImage(user.getImageUrl());
                } catch (Exception e) {
                    // Log warning nhưng vẫn tiếp tục xóa user
                    System.err.println("Warning: Could not delete user image: " + e.getMessage());
                }
            }

            // Xóa user trong database
            service.delete(id);
            return ResponseEntity.noContent().build();
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error deleting user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ❌ XÓA endpoint này - không cần nữa vì ảnh đã lưu trên Cloudinary
    // @GetMapping("/image/{fileName}")
    // public ResponseEntity<InputStreamResource> getImage(@PathVariable String fileName) { ... }
}