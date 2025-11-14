package com.example.cafe.security.services;

import com.example.cafe.entity.User;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User save(User u);
    User update(Long id, User u);
    void delete(Long id);
    Optional<User> findById(Long id);
    List<User> findAll();
    
    String saveImage(MultipartFile file);
    InputStream getImage(String fileName) throws FileNotFoundException;
}