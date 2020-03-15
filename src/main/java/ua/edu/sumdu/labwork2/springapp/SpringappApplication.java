package ua.edu.sumdu.labwork2.springapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ua.edu.sumdu.labwork2.springapp.services.StringToAlbumConverter;

@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
@EnableAsync
@Configuration
public class SpringappApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(SpringappApplication.class, args);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToAlbumConverter());
    }
}
