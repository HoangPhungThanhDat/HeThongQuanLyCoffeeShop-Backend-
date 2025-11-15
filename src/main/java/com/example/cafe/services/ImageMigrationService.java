package com.example.cafe.services;

import com.example.cafe.entity.Product;
import com.example.cafe.repository.ProductRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class ImageMigrationService implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final Cloudinary cloudinary;

    @Value("${project.image:uploads/images/}")
    private String uploadDir;

    public ImageMigrationService(ProductRepository productRepository, 
                                 Cloudinary cloudinary) {
        this.productRepository = productRepository;
        this.cloudinary = cloudinary;
    }

    @Override
    public void run(String... args) throws Exception {
        // ⚠️ CHƯA CHẠY - Để comment như này trước
        // migrateImagesToCloudinary();
    }

    public void migrateImagesToCloudinary() {
        System.out.println("🚀 Starting image migration to Cloudinary...");
        
        List<Product> products = productRepository.findAll();
        int successCount = 0;
        int failCount = 0;

        for (Product product : products) {
            String imageUrl = product.getImageUrl();
            
            if (imageUrl == null || imageUrl.contains("cloudinary.com")) {
                System.out.println("⏭️  Skipping product #" + product.getId() + " - Already using Cloudinary");
                continue;
            }

            try {
                // Đường dẫn file local
                Path imagePath = Paths.get(uploadDir, imageUrl);
                File imageFile = imagePath.toFile();

                if (!imageFile.exists()) {
                    System.out.println("❌ File not found: " + imagePath);
                    failCount++;
                    continue;
                }

                // ✅ Upload trực tiếp file lên Cloudinary (không cần MockMultipartFile)
                Map uploadResult = cloudinary.uploader().upload(imageFile,
                    ObjectUtils.asMap(
                        "folder", "coffee-shop/products",
                        "resource_type", "image"
                    )
                );

                String cloudinaryUrl = (String) uploadResult.get("secure_url");

                // Cập nhật database
                product.setImageUrl(cloudinaryUrl);
                productRepository.save(product);

                System.out.println("✅ Migrated product #" + product.getId() + ": " + product.getName());
                System.out.println("   Old: " + imageUrl);
                System.out.println("   New: " + cloudinaryUrl);
                
                successCount++;

            } catch (Exception e) {
                System.err.println("❌ Failed to migrate product #" + product.getId() + ": " + e.getMessage());
                failCount++;
            }
        }

        System.out.println("\n🎉 Migration completed!");
        System.out.println("✅ Success: " + successCount);
        System.out.println("❌ Failed: " + failCount);
    }
}