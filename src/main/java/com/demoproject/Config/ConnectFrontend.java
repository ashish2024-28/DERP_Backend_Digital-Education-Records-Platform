 package com.demoproject.Config;

 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.config.annotation.CorsRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

 import java.util.Arrays;

 @Configuration
 public class ConnectFrontend {

     @Value("${frontend.url}")
     private String frontendUrl;

    // user for acces backend apis by frontend (react) and also must add in SecurityFilterChain=> .cors(Customizer.withDefaults()) // ✅ Enable CORS inside Spring Security
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

                // Convert comma-separated URLs into array
                String[] allowedOrigins =
                        Arrays.stream(frontendUrl.split(","))
                                .map(String::trim)
                                .filter(url -> !url.isBlank())
                                .toArray(String[]::new);

                registry.addMapping("/**") // Allow all endpoints
                        .allowedOrigins(allowedOrigins) // Frontend / React URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }








 }