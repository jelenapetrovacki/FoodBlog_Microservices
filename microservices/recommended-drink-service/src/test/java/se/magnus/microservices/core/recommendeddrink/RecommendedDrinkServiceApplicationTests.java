package se.magnus.microservices.core.recommendeddrink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.microservices.core.recommendeddrink.persistence.RecommendedDrinkRepository;

@SpringBootTest(webEnvironment=RANDOM_PORT)
class RecommendedDrinkServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private RecommendedDrinkRepository repository;
	
	@BeforeEach
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getRecommendedDrinksByMealId() {

		int mealId = 1;

		postAndVerifyRecommendedDrink(mealId, 1, OK);
		postAndVerifyRecommendedDrink(mealId, 2, OK);
		postAndVerifyRecommendedDrink(mealId, 3, OK);

		assertEquals(3, repository.findByMealId(mealId).size());

		getAndVerifyRecommendedDrinksByMealId(mealId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].mealId").isEqualTo(mealId)
			.jsonPath("$[0].recommendedDrinkId").isEqualTo(1);
	}
	
	@Test
	public void duplicateError() {

		int mealId = 1;
		int recommendedDrinkId = 1;

		postAndVerifyRecommendedDrink(mealId, recommendedDrinkId, OK)
			.jsonPath("$.mealId").isEqualTo(mealId)
			.jsonPath("$.recommendedDrinkId").isEqualTo(recommendedDrinkId);

		assertEquals(1, repository.count());

		postAndVerifyRecommendedDrink(mealId, recommendedDrinkId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/recommendedDrink")
			.jsonPath("$.message").isEqualTo("Duplicate key, Meal Id: 1, Recommended Drink Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteRecommendedDrniks() {

		int mealId = 1;
		int recommendedDrinkId = 1;

		postAndVerifyRecommendedDrink(mealId, recommendedDrinkId, OK);
		assertEquals(1, repository.findByMealId(mealId).size());

		deleteAndVerifyRecommendedDrinksByMealId(mealId, OK);
		assertEquals(0, repository.findByMealId(mealId).size());

		deleteAndVerifyRecommendedDrinksByMealId(mealId, OK);
	}
	
	@Test
	public void getRecommendedDrinksMissingParameter() {
		getAndVerifyRecommendedDrinksByMealId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/recommendedDrink")
			.jsonPath("$.message").isEqualTo("Required int parameter 'mealId' is not present");
	}

	@Test
	public void getRecommendedDrinksInvalidParameter() {
		getAndVerifyRecommendedDrinksByMealId("?mealId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/recommendedDrink")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getRecommendedDrinksNotFound() {
		getAndVerifyRecommendedDrinksByMealId("?mealId=113", OK)
		.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getRecommendedDrinksInvalidParameterNegativeValue() {

		int mealIdInvalId = -1;

		getAndVerifyRecommendedDrinksByMealId("?mealId=" + mealIdInvalId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/recommendedDrink")
			.jsonPath("$.message").isEqualTo("Invalid mealId: " + mealIdInvalId);
	}
	
	//helper methods
	
	private WebTestClient.BodyContentSpec getAndVerifyRecommendedDrinksByMealId(int mealId, HttpStatus expectedStatus) {
		return getAndVerifyRecommendedDrinksByMealId("?mealId=" + mealId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendedDrinksByMealId(String mealIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/recommendedDrink" + mealIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyRecommendedDrink(int mealId, int recommendedDrinkId, HttpStatus expectedStatus) {
		//int mealId, int recommendedDrinkId, String drinkName, String drinkType, boolean nonalcoholic,
		//String glassType, String drinkBrand, String serviceAddress
		RecommendedDrink recommendedDrink = new RecommendedDrink(mealId, recommendedDrinkId, "Name " + recommendedDrinkId, "type", true, "type", "brand", "SA");
		return client.post()
			.uri("/recommendedDrink")
			.body(just(recommendedDrink), RecommendedDrink.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyRecommendedDrinksByMealId(int mealId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/recommendedDrink?mealId=" + mealId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}

}
