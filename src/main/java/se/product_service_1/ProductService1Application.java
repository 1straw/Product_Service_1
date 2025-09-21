package se.product_service_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Lägg till denna annotation
public class ProductService1Application {

    public static void main(String[] args) {
        SpringApplication.run(ProductService1Application.class, args);
    }
}