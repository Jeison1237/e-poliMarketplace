package com.marketplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from the uploads directory
        // Get parent directory (uploads/) to serve all nested paths
        String parentDir = Paths.get(uploadDir).getParent().toAbsolutePath().toUri().toString();
        if (!parentDir.endsWith("/")) {
            parentDir += "/";
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(parentDir)
                .setCachePeriod(3600); // Cache for 1 hour
    }
}
