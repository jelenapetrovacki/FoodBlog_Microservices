package se.magnus.microservices.core.meal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.magnus.api.core.meal.Meal;
import se.magnus.microservices.core.meal.persistence.MealRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static reactor.core.publisher.Mono.just;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class MealServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private MealRepository repository;
	
	@BeforeEach
	public void setupDb() {
		repository.deleteAll();
	}
	
	@Test
	public void getMealById() {

		int mealId = 1;

		postAndVerifyMeal(mealId, OK);
		assertTrue(repository.findByMealId(mealId).isPresent());
		
		getAndVerifyMeal(mealId, OK).jsonPath("$.mealId").isEqualTo(mealId);
	}

	
/*	@Test
	public void duplicateError() {

		int mealId = 1;

		postAndVerifyMeal(mealId, OK);

		assertTrue(repository.findByMealId(mealId).isPresent());

		postAndVerifyMeal(mealId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/meal")
			.jsonPath("$.message").isEqualTo("Duplicate key, Meal Id: " + mealId);
	}
*/
	@Test
	public void deleteMeal() {

		int mealId = 1;

		postAndVerifyMeal(mealId, OK);
		assertTrue(repository.findByMealId(mealId).isPresent());

		deleteAndVerifyMeal(mealId, OK);
		assertFalse(repository.findByMealId(mealId).isPresent());

		deleteAndVerifyMeal(mealId, OK);
	}
	
	@Test
	public void getMealInvalidParameterString() {

		getAndVerifyMeal("/no-integer", BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/meal/no-integer")
		.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getMealNotFound() {

		int mealIdNotFound = 13;

		getAndVerifyMeal(mealIdNotFound, NOT_FOUND)
			.jsonPath("$.path").isEqualTo("/meal/" + mealIdNotFound)
			.jsonPath("$.message").isEqualTo("No meal found for mealId: " + mealIdNotFound);
	}

	@Test
	public void getMealInvalidParameterNegativeValue() {

		int mealIdInvalid = -1;
		
		getAndVerifyMeal(mealIdInvalid, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/meal/" + mealIdInvalid)
				.jsonPath("$.message").isEqualTo("Invalid mealId: " + mealIdInvalid);
	}
	
	// Helper methods
	
	private WebTestClient.BodyContentSpec getAndVerifyMeal(int mealId, HttpStatus expectedStatus) {
		return getAndVerifyMeal("/" + mealId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyMeal(String mealIdPath, HttpStatus expectedStatus) {
		return client.get()
			.uri("/meal" + mealIdPath)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}
	
	private WebTestClient.BodyContentSpec postAndVerifyMeal(int mealId, HttpStatus expectedStatus) {
		//int mealId, String mealName, String category, String reciepeDescription, double calories,String prepartionTime, int serves, String serviceAddress
		Meal meal = new Meal(mealId, "Name " + mealId, "category", "desc", 100, "1h", 2, "SA");
		return client.post()
			.uri("/meal")
			.body(just(meal), Meal.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}
	
	private WebTestClient.BodyContentSpec deleteAndVerifyMeal(int mealId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/meal/" + mealId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}
