package se.magnus.microservices.composite.meal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("se.magnus")
public class MealCompositeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MealCompositeServiceApplication.class, args);
	}

}
