package se.magnus.microservices.composite.meal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

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
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

//@ExtendWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
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
				.thenReturn(new Meal(MEAL_ID_OK, "name", "category", "desc", 100, "1h", 1, "mock-address"));
		when(compositeIntegration.getRecommendedDrinks(MEAL_ID_OK)).thenReturn(singletonList(
				new RecommendedDrink(MEAL_ID_OK, 1, "name", "type", true, "glass", "brand", "mock address")));
		when(compositeIntegration.getComments(MEAL_ID_OK)).thenReturn(
				singletonList(new Comment(MEAL_ID_OK, 1, "author", "subject", "content", null, "mock address")));
		when(compositeIntegration.getIngredients(MEAL_ID_OK)).thenReturn(
				singletonList(new Ingredient(MEAL_ID_OK, 1, "name", 10, "g", "mock address")));
				
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

		client.get().uri("/meal-composite/" + MEAL_ID_OK).accept(APPLICATION_JSON).exchange().expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON).expectBody()
				.jsonPath("$.mealId").isEqualTo(MEAL_ID_OK)
				.jsonPath("$.recommendedDrinks.length()").isEqualTo(1)
				.jsonPath("$.comments.length()").isEqualTo(1)
				.jsonPath("$.ingredients.length()").isEqualTo(1);
	}

	@Test
	public void getMealNotFound() {

		client.get().uri("/meal-composite/" + MEAL_ID_NOT_FOUND).accept(APPLICATION_JSON).exchange().expectStatus()
				.isNotFound().expectHeader().contentType(APPLICATION_JSON).expectBody()
				.jsonPath("$.path").isEqualTo("/meal-composite/" + MEAL_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: " + MEAL_ID_NOT_FOUND);
	}

	@Test
	public void getMealInvalidInput() {

		client.get().uri("/meal-composite/" + MEAL_ID_INVALID).accept(APPLICATION_JSON).exchange().expectStatus()
				.isEqualTo(UNPROCESSABLE_ENTITY).expectHeader().contentType(APPLICATION_JSON).expectBody()
				.jsonPath("$.path").isEqualTo("/meal-composite/" + MEAL_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID: " + MEAL_ID_INVALID);
	}

}
