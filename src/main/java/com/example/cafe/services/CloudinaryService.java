package com.example.cafe.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /** Upload 1 file từ MultipartFile (ảnh mới từ admin) */
    public String uploadFile(MultipartFile file) throws IOException {
        Map result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "Coffee-shop/products",
                        "use_filename", true,       // dùng tên gốc
                        "unique_filename", false,   // KHÔNG thêm suffix
                        "overwrite", true           // ghi đè nếu trùng
                )
        );
        return result.get("secure_url").toString();
    }

    /** Upload 1 file từ đường dẫn local (dùng cho migrate ảnh cũ) */
    public String uploadFile(File file) throws IOException {
        Map result = cloudinary.uploader().upload(
                file,
                ObjectUtils.asMap(
                        "folder", "Coffee-shop/products",
                        "use_filename", true,
                        "unique_filename", false,
                        "overwrite", true
                )
        );
        return result.get("secure_url").toString();
    }
}