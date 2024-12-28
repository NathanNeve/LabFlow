package com.thomasmore.blc.labflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;


@SpringBootApplication
public class LabflowApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.load(); // Load .env file
		System.setProperty("env.DATABASE_USERNAME", dotenv.get("DATABASE_USERNAME"));
		System.setProperty("env.DATABASE_PASSWORD", dotenv.get("DATABASE_PASSWORD"));
		System.setProperty("env.USER_ADMIN_PASSWORD", dotenv.get("USER_ADMIN_PASSWORD"));
		System.setProperty("env.USER_CESAR_PASSWORD", dotenv.get("USER_CESAR_PASSWORD"));
		System.setProperty("env.USER_NATHAN_PASSWORD", dotenv.get("USER_NATHAN_PASSWORD"));

		SpringApplication.run(LabflowApplication.class, args);
	}
}
