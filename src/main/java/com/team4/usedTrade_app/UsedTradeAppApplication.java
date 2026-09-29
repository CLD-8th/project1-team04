package com.team4.usedTrade_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UsedTradeAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsedTradeAppApplication.class, args);
	}
}