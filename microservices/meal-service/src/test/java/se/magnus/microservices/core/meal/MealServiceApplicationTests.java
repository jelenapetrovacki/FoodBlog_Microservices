package se.magnus.microservices.core.meal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment=RANDOM_PORT)
class MealServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void getMealById() {

		int mealId = 1;

		client.get().uri("/meal/" + mealId).accept(APPLICATION_JSON).exchange().expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON).expectBody().jsonPath("$.mealId").isEqualTo(mealId);
	}

	@Test
	public void getMealInvalidParameterString() {

		client.get().uri("/meal/no-integer").accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(BAD_REQUEST).expectHeader().contentType(APPLICATION_JSON).expectBody().jsonPath("$.path")
				.isEqualTo("/meal/no-integer").jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getMealNotFound() {

		int mealIdNotFound = 13;

		client.get().uri("/meal/" + mealIdNotFound).accept(APPLICATION_JSON).exchange().expectStatus()
				.isNotFound().expectHeader().contentType(APPLICATION_JSON).expectBody().jsonPath("$.path")
				.isEqualTo("/meal/" + mealIdNotFound).jsonPath("$.message")
				.isEqualTo("No meal found for mealId: " + mealIdNotFound);
	}

	@Test
	public void getMealInvalidParameterNegativeValue() {

		int mealIdInvalid = -1;

		client.get().uri("/meal/" + mealIdInvalid).accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(UNPROCESSABLE_ENTITY).expectHeader().contentType(APPLICATION_JSON).expectBody()
				.jsonPath("$.path").isEqualTo("/meal/" + mealIdInvalid).jsonPath("$.message")
				.isEqualTo("Invalid mealId: " + mealIdInvalid);
	}
}
