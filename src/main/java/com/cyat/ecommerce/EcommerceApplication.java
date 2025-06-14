package com.cyat.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "Java E-Commerce API is running! Build: " + System.getenv("BUILD_NUMBER");
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/api/products")
    public String products() {
        return "[{\"id\":1,\"name\":\"Laptop\",\"price\":999.99},{\"id\":2,\"name\":\"Phone\",\"price\":699.99}]";
    }
}
