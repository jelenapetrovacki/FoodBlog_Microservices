package se.magnus.microservices.core.recommendeddrink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("se.magnus")
public class RecommendedDrinkServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecommendedDrinkServiceApplication.class, args);
	}

}
