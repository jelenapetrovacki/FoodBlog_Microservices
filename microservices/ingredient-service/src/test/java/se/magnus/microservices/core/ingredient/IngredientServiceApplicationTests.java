package se.magnus.microservices.core.ingredient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment=RANDOM_PORT)
class IngredientServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Test
	public void getIngredientsByMealId() {

		int mealId = 1;

		client.get()
			.uri("/ingredient?mealId=" + mealId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].mealId").isEqualTo(mealId);
	}

	@Test
	public void getIngredientsMissingParameter() {

		client.get()
			.uri("/ingredient")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/ingredient")
			.jsonPath("$.message").isEqualTo("Required int parameter 'mealId' is not present");
	}

	@Test
	public void getIngredientsInvalidParameter() {

		client.get()
			.uri("/ingredient?mealId=no-integer")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/ingredient")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getIngredientsNotFound() {

		int mealIdNotFound = 113;

		client.get()
			.uri("/ingredient?mealId=" + mealIdNotFound)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getIngredientsInvalidParameterNegativeValue() {

		int mealIdInvalId = -1;

		client.get()
			.uri("/ingredient?mealId=" + mealIdInvalId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/ingredient")
			.jsonPath("$.message").isEqualTo("Invalid mealId: " + mealIdInvalId);
	}
}
