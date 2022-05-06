package se.magnus.microservices.core.meal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("se.magnus")
public class MealServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MealServiceApplication.class, args);
	}

}
