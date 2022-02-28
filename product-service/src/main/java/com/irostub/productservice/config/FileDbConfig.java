package com.irostub.productservice.config;

import com.irostub.filedb.FileDb;
import com.irostub.filedb.Mode;
import com.irostub.productservice.domain.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FileDbConfig {
    @Bean
    public FileDb<Product> fileDb() throws IOException {
        return new FileDb<>("productDb", Mode.WRITE, Product.class);
    }
}
