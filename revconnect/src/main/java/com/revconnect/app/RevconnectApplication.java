package com.revconnect.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
@EnableScheduling
public class RevconnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(RevconnectApplication.class, args);
	}

	@Bean
	public CommandLineRunner fixOrphanedDbColumns(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE posts MODIFY reach_count NULL");
				jdbcTemplate.execute("ALTER TABLE posts MODIFY reach_count DEFAULT 0");
				System.out.println("Successfully modified reach_count column constraint.");
			} catch (Exception e) {
				System.out.println(
						"Could not modify reach_count, it might already be fixed or dropped: " + e.getMessage());
			}
			try {
				jdbcTemplate.execute("ALTER TABLE posts MODIFY total_shares_count NULL");
				jdbcTemplate.execute("ALTER TABLE posts MODIFY total_shares_count DEFAULT 0");
				System.out.println("Successfully modified total_shares_count column constraint.");
			} catch (Exception e) {
				System.out.println(
						"Could not modify total_shares_count, it might already be fixed or dropped: " + e.getMessage());
			}
		};
	}
}