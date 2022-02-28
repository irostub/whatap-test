package com.irostub.orderservice.config;

import com.irostub.filedb.FileDb;
import com.irostub.filedb.Mode;
import com.irostub.orderservice.domain.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FileDbConfig {
    @Bean
    public FileDb<Order> fileDb() throws IOException {
        return new FileDb<>("orderDb", Mode.WRITE, Order.class);
    }
}
