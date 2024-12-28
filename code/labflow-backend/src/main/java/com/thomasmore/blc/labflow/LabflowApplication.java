package com.thomasmore.blc.labflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class LabflowApplication implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	public static void main(String[] args) {
		SpringApplication.run(LabflowApplication.class, args);
	}

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment environment = event.getEnvironment();

		// Load environment variables
		String databaseUsername = System.getenv("DATABASE_USERNAME");
		String databasePassword = System.getenv("DATABASE_PASSWORD");
		String adminPassword = System.getenv("USER_ADMIN_PASSWORD");
		String cesarPassword = System.getenv("USER_CESAR_PASSWORD");
		String nathanPassword = System.getenv("USER_NATHAN_PASSWORD");

		// Fallback to .env for local development
		if (databaseUsername == null || databasePassword == null ||
				adminPassword == null || cesarPassword == null || nathanPassword == null) {
			Dotenv dotenv = Dotenv.configure()
					.directory("./") // Adjust path if necessary
					.ignoreIfMissing()
					.load();

			databaseUsername = dotenv.get("DATABASE_USERNAME", "default_username");
			databasePassword = dotenv.get("DATABASE_PASSWORD", "default_password");
			adminPassword = dotenv.get("USER_ADMIN_PASSWORD", "default_admin_password");
			cesarPassword = dotenv.get("USER_CESAR_PASSWORD", "default_cesar_password");
			nathanPassword = dotenv.get("USER_NATHAN_PASSWORD", "default_nathan_password");
		}

		// Add properties to the Spring Environment
		Map<String, Object> propertySource = new HashMap<>();
		propertySource.put("user.admin.password", adminPassword);
		propertySource.put("user.cesar.password", cesarPassword);
		propertySource.put("user.nathan.password", nathanPassword);
		propertySource.put("spring.datasource.username", databaseUsername);
		propertySource.put("spring.datasource.password", databasePassword);

		environment.getPropertySources().addFirst(new MapPropertySource("customProperties", propertySource));
	}
}
