package com.mbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching // Add this annotation to enable caching support
@EnableAsync
public class MBankApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MBankApiApplication.class, args);
	}

}
