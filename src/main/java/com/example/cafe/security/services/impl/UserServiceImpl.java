package com.example.cafe.security.services.impl;

import com.example.cafe.entity.User;
import com.example.cafe.repository.UserRepository;
import com.example.cafe.security.services.UserService;
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
    private final PasswordEncoder passwordEncoder; // ✅ THÊM DÒNG NÀY

    @Value("${project.image}")
    private String uploadDir;

    
    public UserServiceImpl(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

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
        
        return repo.save(u);
    }

    @Override
    public User update(Long id, User u) {
        return repo.findById(id).map(existing -> {
           
            if (u.getPassword() != null && !u.getPassword().isEmpty()) {
                existing.setPassword(passwordEncoder.encode(u.getPassword()));
            }
            
            existing.setFullName(u.getFullName());
            existing.setRole(u.getRole());
            
           
            if (u.getEmail() != null && !u.getEmail().equals(existing.getEmail())) {
                if (repo.existsByEmail(u.getEmail())) {
                    throw new RuntimeException("Email đã được sử dụng bởi user khác!");
                }
                existing.setEmail(u.getEmail());
            }
            
            existing.setPhone(u.getPhone());
            existing.setIsActive(u.getIsActive());
            
            // Chỉ cập nhật imageUrl nếu có giá trị mới
            if (u.getImageUrl() != null) {
                existing.setImageUrl(u.getImageUrl());
            }
            
            return repo.save(existing);
        }).orElseThrow(() -> new RuntimeException("User not found với ID: " + id));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
throw new RuntimeException("User not found với ID: " + id);
        }
        repo.deleteById(id);
    }

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
