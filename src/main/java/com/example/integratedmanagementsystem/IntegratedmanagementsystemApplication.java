package com.example.integratedmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IntegratedmanagementsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntegratedmanagementsystemApplication.class, args);
	}

}
