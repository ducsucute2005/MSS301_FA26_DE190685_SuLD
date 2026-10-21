package com.mss301.inventory.config;

import com.mss301.inventory.entity.Product;
import com.mss301.inventory.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    // MongoDB khong co data.sql -> nap du lieu mau khi khoi dong, chi nap neu con trong
    @Bean
    CommandLineRunner seedProducts(ProductRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.saveAll(List.of(
                        new Product("P1", "Laptop", 1000, 10),
                        new Product("P2", "Mouse", 20, 5)));
            }
        };
    }
}
