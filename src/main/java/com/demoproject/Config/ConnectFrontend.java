 package com.demoproject.Config;

 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.web.servlet.config.annotation.CorsRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

 @Configuration
 public class ConnectFrontend {

    // user for acces backend apis by frontend (react) and also must add in SecurityFilterChain=> .cors(Customizer.withDefaults()) // ✅ Enable CORS inside Spring Security
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Value("${frontend.url}")
            private String frontendUrl;
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Allow all endpoints
                        .allowedOrigins(frontendUrl) // Your React URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }








 }