package com.sanosysalvos.bff.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BffApplication {

	public static void main(String[] args) {
		SpringApplication.run(BffApplication.class, args);
	}

	// es para llamar a ms-pets y ms-users
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
