package com.project.mentor_booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({ "com.project.controller","com.project.service" })
@EnableJpaRepositories(basePackages = "com.project.repository")
@EntityScan(basePackages = "com.project.model")
public class MentorBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(MentorBookingApplication.class, args);
	}

}