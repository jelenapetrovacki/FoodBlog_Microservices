package se.magnus.microservices.core.comment;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment=RANDOM_PORT)
class CommentServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void getCommentsByMealId() {

		int mealId = 1;

		client.get()
			.uri("/comment?mealId=" + mealId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].mealId").isEqualTo(mealId);
	}

	@Test
	public void getCommentsMissingParameter() {

		client.get()
			.uri("/comment")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Required int parameter 'mealId' is not present");
	}

	@Test
	public void getCommentsInvalidParameter() {

		client.get()
			.uri("/comment?mealId=no-integer")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getCommentsNotFound() {

		int mealIdNotFound = 113;

		client.get()
			.uri("/comment?mealId=" + mealIdNotFound)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getCommentsInvalidParameterNegativeValue() {

		int mealIdInvalId = -1;

		client.get()
			.uri("/comment?mealId=" + mealIdInvalId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Invalid mealId: " + mealIdInvalId);
	}
	
}
