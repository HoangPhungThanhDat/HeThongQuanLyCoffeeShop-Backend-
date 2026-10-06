// package com.example.cafe.security.services.impl;

// import com.example.cafe.entity.User;
// import com.example.cafe.repository.UserRepository;
// import com.example.cafe.security.services.UserService;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;
// import org.springframework.util.StringUtils;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.*;
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.nio.file.StandardCopyOption;
// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// @Service
// public class UserServiceImpl implements UserService {
//     private final UserRepository repo;
//     private final PasswordEncoder passwordEncoder; // ✅ THÊM DÒNG NÀY

//     @Value("${project.image}")
//     private String uploadDir;

    
//     public UserServiceImpl(UserRepository repo, PasswordEncoder passwordEncoder) {
//         this.repo = repo;
//         this.passwordEncoder = passwordEncoder;
//     }

//     @Override
//     public User save(User u) {
   
//         if (repo.existsByUsername(u.getUsername())) {
//             throw new RuntimeException("Username đã tồn tại!");
//         }
        
   
//         if (u.getEmail() != null && repo.existsByEmail(u.getEmail())) {
//             throw new RuntimeException("Email đã tồn tại!");
//         }
        
       
//         if (u.getPassword() != null && !u.getPassword().isEmpty()) {
//             u.setPassword(passwordEncoder.encode(u.getPassword()));
//         } else {
//             throw new RuntimeException("Password không được để trống!");
//         }
        
//         return repo.save(u);
//     }

//     @Override
//     public User update(Long id, User u) {
//         return repo.findById(id).map(existing -> {
           
//             if (u.getPassword() != null && !u.getPassword().isEmpty()) {
//                 existing.setPassword(passwordEncoder.encode(u.getPassword()));
//             }
            
//             existing.setFullName(u.getFullName());
//             existing.setRole(u.getRole());
            
           
//             if (u.getEmail() != null && !u.getEmail().equals(existing.getEmail())) {
//                 if (repo.existsByEmail(u.getEmail())) {
//                     throw new RuntimeException("Email đã được sử dụng bởi user khác!");
//                 }
//                 existing.setEmail(u.getEmail());
//             }
            
//             existing.setPhone(u.getPhone());
//             existing.setIsActive(u.getIsActive());
            
//             // Chỉ cập nhật imageUrl nếu có giá trị mới
//             if (u.getImageUrl() != null) {
//                 existing.setImageUrl(u.getImageUrl());
//             }
            
//             return repo.save(existing);
//         }).orElseThrow(() -> new RuntimeException("User not found với ID: " + id));
//     }

//     @Override
//     public void delete(Long id) {
//         if (!repo.existsById(id)) {
// throw new RuntimeException("User not found với ID: " + id);
//         }
//         repo.deleteById(id);
//     }

//     @Override
//     public Optional<User> findById(Long id) {
//         return repo.findById(id);
//     }

//     @Override
//     public List<User> findAll() {
//         return repo.findAll();
//     }

//     @Override
//     public String saveImage(MultipartFile file) {
//         File dir = new File(uploadDir);
//         if (!dir.exists()) dir.mkdirs();

//         String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
//         String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
//         String fileName = UUID.randomUUID().toString() + fileExtension;

//         try {
//             Files.copy(file.getInputStream(),
//                     Paths.get(uploadDir + File.separator + fileName),
//                     StandardCopyOption.REPLACE_EXISTING);
//         } catch (IOException e) {
//             throw new RuntimeException("Lỗi lưu file ảnh: " + e.getMessage());
//         }

//         return fileName;
//     }

//     @Override
//     public InputStream getImage(String fileName) throws FileNotFoundException {
//         String fullPath = uploadDir + File.separator + fileName;
//         File file = new File(fullPath);
//         if (!file.exists()) {
//             throw new FileNotFoundException("Không tìm thấy file: " + fileName);
//         }
//         return new FileInputStream(fullPath);
//     }
// }

















package com.example.cafe.security.services.impl;

import com.example.cafe.entity.User;
import com.example.cafe.entity.enums.LogAction;
import com.example.cafe.entity.enums.LogLevel;
import com.example.cafe.repository.UserRepository;
import com.example.cafe.security.services.UserService;
import com.example.cafe.services.ActivityLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;   // 👈 MỚI

    @Value("${project.image}")
    private String uploadDir;

    public UserServiceImpl(UserRepository repo,
                           PasswordEncoder passwordEncoder,
                           ActivityLogService activityLogService) {   // 👈 MỚI
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    // ==================== SAVE (CREATE) ====================
    @Override
    public User save(User u) {
        if (repo.existsByUsername(u.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        if (u.getEmail() != null && repo.existsByEmail(u.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        if (u.getPassword() != null && !u.getPassword().isEmpty()) {
            u.setPassword(passwordEncoder.encode(u.getPassword()));
        } else {
            throw new RuntimeException("Password không được để trống!");
        }

        User saved = repo.save(u);

        // ✅ GHI LOG
        try {
            activityLogService.logCreate(
                    "Người dùng",
                    saved.getUsername(),
                    "Tạo tài khoản mới",
                    String.format("Tạo tài khoản '%s' — vai trò %s, email %s",
                            saved.getUsername(),
                            saved.getRole(),
                            saved.getEmail() != null ? saved.getEmail() : "chưa có")
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log CREATE user: " + e.getMessage());
        }

        return saved;
    }

    // ==================== UPDATE ====================
    @Override
    public User update(Long id, User u) {
        User existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found với ID: " + id));

        // Snapshot để log
        String oldFullName = existing.getFullName();
        String oldEmail = existing.getEmail();
        String oldPhone = existing.getPhone();
        Object oldRole = existing.getRole();
        Boolean oldIsActive = existing.getIsActive();

        User updated = repo.findById(id).map(entity -> {
            if (u.getPassword() != null && !u.getPassword().isEmpty()) {
                entity.setPassword(passwordEncoder.encode(u.getPassword()));
            }

            entity.setFullName(u.getFullName());
            entity.setRole(u.getRole());

            if (u.getEmail() != null && !u.getEmail().equals(entity.getEmail())) {
                if (repo.existsByEmail(u.getEmail())) {
                    throw new RuntimeException("Email đã được sử dụng bởi user khác!");
                }
                entity.setEmail(u.getEmail());
            }

            entity.setPhone(u.getPhone());
            entity.setIsActive(u.getIsActive());

            if (u.getImageUrl() != null) {
                entity.setImageUrl(u.getImageUrl());
            }

            return repo.save(entity);
        }).orElseThrow(() -> new RuntimeException("User not found với ID: " + id));

        // ✅ GHI LOG
        try {
            StringBuilder changes = new StringBuilder();
            if (u.getPassword() != null && !u.getPassword().isEmpty()) {
                changes.append("Đổi mật khẩu. ");
            }
            if (oldFullName != null && !oldFullName.equals(updated.getFullName())) {
                changes.append("Tên: '").append(oldFullName).append("' → '")
                        .append(updated.getFullName()).append("'. ");
            }
            if (oldEmail != null && !oldEmail.equals(updated.getEmail())) {
                changes.append("Email: '").append(oldEmail).append("' → '")
                        .append(updated.getEmail()).append("'. ");
            }
            if (oldPhone != null && !oldPhone.equals(updated.getPhone())) {
                changes.append("SĐT: ").append(oldPhone).append(" → ")
                        .append(updated.getPhone()).append(". ");
            }
            if (oldRole != null && !oldRole.equals(updated.getRole())) {
                changes.append("Vai trò: ").append(oldRole).append(" → ")
                        .append(updated.getRole()).append(". ");
            }
            if (oldIsActive != null && !oldIsActive.equals(updated.getIsActive())) {
                changes.append("Trạng thái: ")
                        .append(oldIsActive ? "Hoạt động" : "Khóa")
                        .append(" → ")
                        .append(updated.getIsActive() ? "Hoạt động" : "Khóa")
                        .append(". ");
            }

            activityLogService.logUpdate(
                    "Người dùng",
                    updated.getUsername(),
                    "Cập nhật thông tin người dùng",
                    changes.length() > 0
                            ? changes.toString().trim()
                            : "Cập nhật user '" + updated.getUsername() + "'"
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log UPDATE user: " + e.getMessage());
        }

        return updated;
    }

    // ==================== DELETE ====================
    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("User not found với ID: " + id);
        }

        User user = repo.findById(id).orElse(null);
        String username = user != null ? user.getUsername() : "ID " + id;
        String role = user != null && user.getRole() != null ? user.getRole().name() : "N/A";

        repo.deleteById(id);

        // ✅ GHI LOG (dùng WARNING vì xóa user là hành động nguy hiểm)
        try {
            activityLogService.saveLog(
                    LogAction.DELETE,
                    LogLevel.WARNING,
                    "Người dùng",
                    username,
                    "Xóa tài khoản người dùng",
                    String.format("Đã xóa tài khoản '%s' (vai trò %s) khỏi hệ thống",
                            username, role)
            );
        } catch (Exception e) {
            System.err.println("⚠️ Không ghi được log DELETE user: " + e.getMessage());
        }
    }

    // ==================== CÁC METHOD CÒN LẠI ====================

    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<User> findAll() {
        return repo.findAll();
    }

    @Override
    public String saveImage(MultipartFile file) {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Files.copy(file.getInputStream(),
                    Paths.get(uploadDir + File.separator + fileName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu file ảnh: " + e.getMessage());
        }

        return fileName;
    }

    @Override
    public InputStream getImage(String fileName) throws FileNotFoundException {
        String fullPath = uploadDir + File.separator + fileName;
        File file = new File(fullPath);
        if (!file.exists()) {
            throw new FileNotFoundException("Không tìm thấy file: " + fileName);
        }
        return new FileInputStream(fullPath);
    }
}