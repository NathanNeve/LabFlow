package com.thomasmore.blc.labflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;


@SpringBootApplication
public class LabflowApplication {

	public static void main(String[] args) {
		// Use System.getenv() to fetch variables set in production
		String databaseUsername = System.getenv("DATABASE_USERNAME");
		String databasePassword = System.getenv("DATABASE_PASSWORD");
		String adminPassword = System.getenv("USER_ADMIN_PASSWORD");
		String cesarPassword = System.getenv("USER_CESAR_PASSWORD");
		String nathanPassword = System.getenv("USER_NATHAN_PASSWORD");

		// Fallback for development environments where a .env file is used
		if (databaseUsername == null || databasePassword == null ||
				adminPassword == null || cesarPassword == null || nathanPassword == null) {
			Dotenv dotenv = Dotenv.configure()
					.directory("./") // Adjust path if necessary
					.ignoreIfMissing() // Avoid exceptions if .env doesn't exist
					.load();

			databaseUsername = dotenv.get("DATABASE_USERNAME", "default_username");
			databasePassword = dotenv.get("DATABASE_PASSWORD", "default_password");
			adminPassword = dotenv.get("USER_ADMIN_PASSWORD", "default_admin_password");
			cesarPassword = dotenv.get("USER_CESAR_PASSWORD", "default_cesar_password");
			nathanPassword = dotenv.get("USER_NATHAN_PASSWORD", "default_nathan_password");
		}

		// Set System properties
		System.setProperty("env.DATABASE_USERNAME", databaseUsername);
		System.setProperty("env.DATABASE_PASSWORD", databasePassword);
		System.setProperty("env.USER_ADMIN_PASSWORD", adminPassword);
		System.setProperty("env.USER_CESAR_PASSWORD", cesarPassword);
		System.setProperty("env.USER_NATHAN_PASSWORD", nathanPassword);

		// Start the application
		SpringApplication.run(LabflowApplication.class, args);
	}
}
