package se.magnus.microservices.core.recommendeddrink;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment=RANDOM_PORT)
class RecommendedDrinkServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Test
	public void getRecommendedDrinksByMealId() {

		int mealId = 1;

		client.get()
			.uri("/recommendedDrink?mealId=" + mealId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].mealId").isEqualTo(mealId);
	}

	@Test
	public void getRecommendedDrinksMissingParameter() {

		client.get()
			.uri("/recommendedDrink")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/recommendedDrink")
			.jsonPath("$.message").isEqualTo("Required int parameter 'mealId' is not present");
	}

	@Test
	public void getRecommendedDrinksInvalidParameter() {

		client.get()
			.uri("/recommendedDrink?mealId=no-integer")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/recommendedDrink")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getRecommendedDrinksNotFound() {

		int mealIdNotFound = 113;

		client.get()
			.uri("/recommendedDrink?mealId=" + mealIdNotFound)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getRecommendedDrinksInvalidParameterNegativeValue() {

		int mealIdInvalId = -1;

		client.get()
			.uri("/recommendedDrink?mealId=" + mealIdInvalId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/recommendedDrink")
			.jsonPath("$.message").isEqualTo("Invalid mealId: " + mealIdInvalId);
	}

}
