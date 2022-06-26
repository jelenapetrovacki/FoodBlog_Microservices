package se.magnus.microservices.core.recommendeddrink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;
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

import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.recommendeddrink.persistence.RecommendedDrinkRepository;
import se.magnus.util.exceptions.InvalidInputException;

@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class RecommendedDrinkServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private RecommendedDrinkRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@BeforeEach
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll();
	}

	@Test
	public void getRecommendedDrinksByMealId() {

		int mealId = 1;

		sendCreateRecommendedDrinkEvent(mealId, 1);
		sendCreateRecommendedDrinkEvent(mealId, 2);
		sendCreateRecommendedDrinkEvent(mealId, 3);

		assertEquals(3, (long) repository.findByMealId(mealId).count().block());

		getAndVerifyRecommendedDrinksByMealId(mealId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].mealId").isEqualTo(mealId)
			.jsonPath("$[0].recommendedDrinkId").isEqualTo(1);
	}
	
	/*@Test
	public void duplicateError() {

		int mealId = 1;
		int recommendedDrinkId = 1;

		sendCreateRecommendedDrinkEvent(mealId, recommendedDrinkId);

		assertEquals(1, (long)repository.count().block());

		try {
			sendCreateRecommendedDrinkEvent(mealId, recommendedDrinkId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Meal Id: 1, RecommendedDrink Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, (long)repository.count().block());
	}
*/
	@Test
	public void deleteRecommendedDrinks() {

		int mealId = 1;
		int recommendedDrinkId = 1;

		sendCreateRecommendedDrinkEvent(mealId, recommendedDrinkId);
		assertEquals(1, (long) repository.findByMealId(mealId).count().block());

		sendDeleteRecommendedDrinkEvent(mealId);
		assertEquals(0, (long) repository.findByMealId(mealId).count().block());

		sendDeleteRecommendedDrinkEvent(mealId);
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

	private void sendCreateRecommendedDrinkEvent(int mealId, int recommendedDrinkId) {
		RecommendedDrink recommendedDrink = new RecommendedDrink(mealId, recommendedDrinkId, "Name " + recommendedDrinkId, "type", true, "type", "brand", "SA");
		Event<Integer, Meal> event = new Event(CREATE, mealId, recommendedDrink);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteRecommendedDrinkEvent(int mealId) {
		Event<Integer, Meal> event = new Event(DELETE, mealId, null);
		input.send(new GenericMessage<>(event));
	}

}
