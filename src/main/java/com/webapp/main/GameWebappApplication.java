package com.webapp.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.webapp")
public class GameWebappApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameWebappApplication.class, args);
	}

}
