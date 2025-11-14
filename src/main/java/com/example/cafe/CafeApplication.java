package com.example.cafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling // Bật Scheduler
public class CafeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeApplication.class, args);
        
        System.out.println("\n ================================================");
        System.out.println(" CAFE APPLICATION STARTED SUCCESSFULLY!");
        System.out.println("================================================");
        System.out.println(" Backend API: http://localhost:8080");
        System.out.println(" Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println(" Socket Server: http://localhost:3001");
        System.out.println(" Frontend: http://localhost:3000");
        System.out.println(" ================================================\n");
    }

    /**
     * Bean để gọi HTTP requests đến Socket Server
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}