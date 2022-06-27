package se.magnus.microservices.core.meal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.meal.persistence.MealRepository;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.channel.AbstractMessageChannel;
import se.magnus.util.exceptions.InvalidInputException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client.enabled=false", "spring.data.mongodb.port: 0"})
class MealServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private MealRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@BeforeEach
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}
	
	@Test
	public void getMealById() {

		int mealId = 1;

		assertNull(repository.findByMealId(mealId).block());
		assertEquals(0, (long)repository.count().block());

		sendCreateMealEvent(mealId);

		assertNotNull(repository.findByMealId(mealId).block());
		assertEquals(1, (long)repository.count().block());
		
		getAndVerifyMeal(mealId, OK).jsonPath("$.mealId").isEqualTo(mealId);
	}
/*
	@Test
	public void duplicateError() {

		int mealId = 1;

		assertNull(repository.findByMealId(mealId).block());
		sendCreateMealEvent(mealId);
		assertNotNull(repository.findByMealId(mealId).block());

		try {
			sendCreateMealEvent(mealId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Meal Id: " + mealId, iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}
	}
*/
	@Test
	public void deleteMeal() {

		int mealId = 1;

		sendCreateMealEvent(mealId);
		assertNotNull(repository.findByMealId(mealId).block());

		sendDeleteMealEvent(mealId);
		assertNull(repository.findByMealId(mealId).block());

		sendDeleteMealEvent(mealId);
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
	

	private void sendCreateMealEvent(int mealId) {
		Meal meal = new Meal(mealId, "Name " + mealId, "category", "desc",
				100, "1h", 2, "SA");


		Event<Integer, Meal> event = new Event(CREATE, mealId, meal);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteMealEvent(int mealId) {
		Event<Integer, Meal> event = new Event(DELETE, mealId, null);
		input.send(new GenericMessage<>(event));
	}
}
