package com.demoproject.Config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/university/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    
   @Bean
   public ModelMapper modelMapper() {
       ModelMapper mapper = new ModelMapper();

       mapper.getConfiguration()
           .setMatchingStrategy(MatchingStrategies.STRICT)
           .setSkipNullEnabled(true)
           .setFieldMatchingEnabled(true)
           .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

       return mapper;
   }

}
