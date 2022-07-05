package se.magnus.microservices.core.ingredient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.event.Event;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.meal.Meal;
import se.magnus.microservices.core.ingredient.persistence.IngredientRepository;
import se.magnus.util.exceptions.InvalidInputException;


@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {
		"eureka.client.enabled=false",
		"spring.data.mongodb.port: 0", "spring.cloud.config.enabled=false", "server.error.include-message=always"})
class IngredientServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private IngredientRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@BeforeEach
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll();
	}

	
	@Test
	public void getIngredientsByMealId() {

		int mealId = 1;
		repository.deleteAll();
		sendCreateIngredientEvent(mealId, 1);
		sendCreateIngredientEvent(mealId, 2);
		sendCreateIngredientEvent(mealId, 3);

		assertEquals(3, (long) repository.findByMealId(mealId).count().block());

		getAndVerifyIngredientsByMealId(mealId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].mealId").isEqualTo(mealId)
			.jsonPath("$[0].ingredientId").isEqualTo(1);
	}

/*	@Test
	public void duplicateError() {

		int mealId = 1;
		int ingredientId = 1;

		sendCreateIngredientEvent(mealId, ingredientId);

		assertEquals(1, (long)repository.count().block());

		try {
			sendCreateIngredientEvent(mealId, ingredientId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Meal Id: 1, Ingredient Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, (long)repository.count().block());
	}
*/
	@Test
	public void deleteIngredients() {

		int mealId = 1;
		int ingredientId = 1;

		sendCreateIngredientEvent(mealId, ingredientId);
		assertEquals(1, (long) repository.findByMealId(mealId).count().block());

		sendDeleteIngredientEvent(mealId);
		assertEquals(0, (long) repository.findByMealId(mealId).count().block());

		sendDeleteIngredientEvent(mealId);
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

	private void sendCreateIngredientEvent(int mealId, int ingredientId) {
		Ingredient ingredient = new Ingredient(mealId, ingredientId, "Name " + ingredientId, 1, "kg", "SA");
		Event<Integer, Meal> event = new Event(CREATE, mealId, ingredient);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteIngredientEvent(int mealId) {
		Event<Integer, Meal> event = new Event(DELETE, mealId, null);
		input.send(new GenericMessage<>(event));
	}
}
