package com.palash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.palash.service.UniqueTrackingIdGenerator;

@SpringBootApplication
public class IdGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdGeneratorApplication.class, args);
		for (int i = 0; i < 10; i++) {
			String customObjectId = UniqueTrackingIdGenerator.generateId();
	        System.out.println("Generated ID: " + customObjectId);
		}
		
	}

}
