package se.magnus.microservices.composite.meal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.meal.CommentSummary;
import se.magnus.api.composite.meal.IngredientSummary;
import se.magnus.api.composite.meal.MealAggregate;
import se.magnus.api.composite.meal.RecommendedDrinkSummary;
import se.magnus.api.composite.meal.ServiceAddresses;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.microservices.composite.meal.services.MealCompositeIntegration;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.util.List;

//@ExtendWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment=RANDOM_PORT,
		classes = {MealCompositeServiceApplication.class, TestSecurityConfig.class },
		properties = {"spring.main.allow-bean-definition-overriding=true","eureka.client.enabled=false"})
class MealCompositeServiceApplicationTests {

	private static final int MEAL_ID_OK = 1;
	private static final int MEAL_ID_NOT_FOUND = 2;
	private static final int MEAL_ID_INVALID = 3;

	@Autowired
	private WebTestClient client;

	@MockBean
	private MealCompositeIntegration compositeIntegration;

	@BeforeEach
	public void setUp() {

		when(compositeIntegration.getMeal(MEAL_ID_OK))
				.thenReturn(Mono.just(new Meal(MEAL_ID_OK, "name", "category",
						"desc", 100, "1h", 1, "mock-address")));
		when(compositeIntegration.getRecommendedDrinks(MEAL_ID_OK)).thenReturn(
				Flux.fromIterable(singletonList(
				new RecommendedDrink(MEAL_ID_OK, 1, "name", "type",
						true, "glass", "brand", "mock address"))));
		when(compositeIntegration.getComments(MEAL_ID_OK)).thenReturn(
				Flux.fromIterable(singletonList(new Comment(MEAL_ID_OK, 1, "author", "subject",
						"content", null, "mock address"))));
		when(compositeIntegration.getIngredients(MEAL_ID_OK)).thenReturn(
				Flux.fromIterable(singletonList(new Ingredient(MEAL_ID_OK, 1, "name", 10,
						"g", "mock address"))));
				
		when(compositeIntegration.getMeal(MEAL_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: " + MEAL_ID_NOT_FOUND));

		when(compositeIntegration.getMeal(MEAL_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID: " + MEAL_ID_INVALID));
	}

	@Test
	void contextLoads() {
	}


	@Test
	public void getMealById() {

		getAndVerifyMeal(MEAL_ID_OK, OK)
				.jsonPath("$.mealId").isEqualTo(MEAL_ID_OK)
				.jsonPath("$.recommendedDrinks.length()").isEqualTo(1)
				.jsonPath("$.comments.length()").isEqualTo(1)
				.jsonPath("$.ingredients.length()").isEqualTo(1);
	}

	@Test
	public void getMealNotFound() {

		getAndVerifyMeal(MEAL_ID_NOT_FOUND, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/meal-composite/" + MEAL_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: " + MEAL_ID_NOT_FOUND);
	}

	@Test
	public void getMealInvalidInput() {

		getAndVerifyMeal(MEAL_ID_INVALID, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/meal-composite/" + MEAL_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID: " + MEAL_ID_INVALID);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyMeal(int mealId, HttpStatus expectedStatus) {
		return client.get()
			.uri("/meal-composite/" + mealId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}
}
