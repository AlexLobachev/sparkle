package com.example.sparkle.sparkle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;

@SpringBootApplication
public class SparkleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SparkleApplication.class, args);
	}

}
