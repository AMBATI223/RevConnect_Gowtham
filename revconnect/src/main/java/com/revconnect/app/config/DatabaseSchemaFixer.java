package com.revconnect.app.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseSchemaFixer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaFixer.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("Checking for missing database columns...");

        // Add reach_count to posts
        try {
            jdbcTemplate.execute("ALTER TABLE posts ADD reach_count NUMBER(19,0) DEFAULT 0");
            logger.info("Added reach_count column to posts table.");
        } catch (Exception e) {
            logger.debug("reach_count column might already exist: {}", e.getMessage());
        }

        // Add shares_count to posts
        try {
            jdbcTemplate.execute("ALTER TABLE posts ADD shares_count NUMBER(19,0) DEFAULT 0");
            logger.info("Added shares_count column to posts table.");
        } catch (Exception e) {
            logger.debug("shares_count column might already exist: {}", e.getMessage());
        }

        // Add product_id to posts (for tagged products)
        try {
            jdbcTemplate.execute("ALTER TABLE posts ADD product_id NUMBER(19,0)");
            logger.info("Added product_id column to posts table.");
        } catch (Exception e) {
            logger.debug("product_id column might already exist: {}", e.getMessage());
        }

        logger.info("Database schema check completed.");
    }
}
