package com.example.clubmanagementsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.clubmanagementsystem.mapper")
public class ClubManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClubManagementSystemApplication.class, args);
    }

}
