package se.magnus.microservices.core.ingredient;

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
import se.magnus.microservices.core.ingredient.persistence.IngredientRepository;

@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class IngredientServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private IngredientRepository repository; 
	
	@BeforeEach
	public void setupDb() {
		repository.deleteAll();
	}

	
	@Test
	public void getIngredientsByMealId() {

		int mealId = 1;

		postAndVerifyIngredient(mealId, 1, OK);
		postAndVerifyIngredient(mealId, 2, OK);
		postAndVerifyIngredient(mealId, 3, OK);

		assertEquals(3, repository.findByMealId(mealId).size());

		getAndVerifyIngredientsByMealId(mealId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].mealId").isEqualTo(mealId)
			.jsonPath("$[0].ingredientId").isEqualTo(1);
	}

/*	@Test
	public void duplicateError() {

		int mealId = 1;
		int ingredientId = 1;

		postAndVerifyIngredient(mealId, ingredientId, OK)
			.jsonPath("$.mealId").isEqualTo(mealId)
			.jsonPath("$.ingredientId").isEqualTo(ingredientId);

		assertEquals(1, repository.count());

		postAndVerifyIngredient(mealId, ingredientId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/ingredient")
			.jsonPath("$.message").isEqualTo("Duplicate key, Meal Id: 1, Ingredient Id:1");

		assertEquals(1, repository.count());
	}
*/
	@Test
	public void deleteIngredients() {

		int mealId = 1;
		int ingredientId = 1;

		postAndVerifyIngredient(mealId, ingredientId, OK);
		assertEquals(1, repository.findByMealId(mealId).size());

		deleteAndVerifyIngredientsByMealId(mealId, OK);
		assertEquals(0, repository.findByMealId(mealId).size());

		deleteAndVerifyIngredientsByMealId(mealId, OK);
	}
	
	@Test
	public void getIngredientsMissingParameter() {

		getAndVerifyIngredientsByMealId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/ingredient")
			.jsonPath("$.message").isEqualTo("Required int parameter 'mealId' is not present");
	}

	@Test
	public void getIngredientsInvalidParameter() {

		getAndVerifyIngredientsByMealId("?mealId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/ingredient")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getIngredientsNotFound() {

		getAndVerifyIngredientsByMealId("?mealId=113", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getIngredientsInvalidParameterNegativeValue() {

		int mealIdInvalId = -1;

		getAndVerifyIngredientsByMealId("?mealId=" + mealIdInvalId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/ingredient")
			.jsonPath("$.message").isEqualTo("Invalid mealId: " + mealIdInvalId);
		
	}
	
	//helper methods
	
	private WebTestClient.BodyContentSpec getAndVerifyIngredientsByMealId(int mealId, HttpStatus expectedStatus) {
		return getAndVerifyIngredientsByMealId("?mealId=" + mealId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyIngredientsByMealId(String mealIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/ingredient" + mealIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyIngredient(int mealId, int ingredientId, HttpStatus expectedStatus) {
		//int mealId, int ingredientId, String name, int amount, String unitOfMeasure, String serviceAddress
		Ingredient ingredient = new Ingredient(mealId, ingredientId, "Name " + ingredientId, 1, "kg", "SA");
		return client.post()
			.uri("/ingredient")
			.body(just(ingredient), Ingredient.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyIngredientsByMealId(int mealId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/ingredient?mealId=" + mealId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}
