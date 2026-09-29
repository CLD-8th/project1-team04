package com.team4.usedtradeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UsedTradeAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsedTradeAppApplication.class, args);
	}

}
