package com.revconnect.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class RevconnectApplication {

	private static final Logger log = LoggerFactory.getLogger(RevconnectApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RevconnectApplication.class, args);
	}
}