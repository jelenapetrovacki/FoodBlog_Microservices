package se.magnus.microservices.core.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

import java.util.Date;

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

import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.api.event.Event;
import se.magnus.microservices.core.comment.persistence.CommentRepository;
import se.magnus.util.exceptions.InvalidInputException;

@SpringBootTest(webEnvironment=RANDOM_PORT,properties = {
"spring.datasource.url=jdbc:h2:mem:review-db"})
class CommentServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private CommentRepository repository;

	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@BeforeEach
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll();
	}

	@Test
	public void getCommentsByMealId() {

		int mealId = 1;

		assertEquals(0, repository.findByMealId(mealId).size());

		sendCreateCommentEvent(mealId, 1);
		sendCreateCommentEvent(mealId, 2);
		sendCreateCommentEvent(mealId, 3);

		assertEquals(3, repository.findByMealId(mealId).size());

		getAndVerifyCommentsByMealId(mealId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].mealId").isEqualTo(mealId)
			.jsonPath("$[2].commentId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {

		int mealId = 1;
		int commentId = 1;

		assertEquals(0, repository.count());

		sendCreateCommentEvent(mealId, commentId);

		assertEquals(1, repository.count());

		try {
			sendCreateCommentEvent(mealId, commentId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Meal Id: 1, Comment Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteComments() {

		int mealId = 1;
		int commentId = 1;

		sendCreateCommentEvent(mealId, commentId);
		assertEquals(1, repository.findByMealId(mealId).size());

		sendDeleteCommentEvent(mealId);
		assertEquals(0, repository.findByMealId(mealId).size());

		sendDeleteCommentEvent(mealId);
	}
	
	@Test
	public void getCommentsMissingParameter() {

		getAndVerifyCommentsByMealId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Required int parameter 'mealId' is not present");
	}

	@Test
	public void getCommentsInvalidParameter() {

		getAndVerifyCommentsByMealId("?mealId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getCommentsNotFound() {

		getAndVerifyCommentsByMealId("?mealId=113", OK)
		.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getCommentsInvalidParameterNegativeValue() {

		int mealIdInvalId = -1;
		getAndVerifyCommentsByMealId("?mealId=" + mealIdInvalId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Invalid mealId: " + mealIdInvalId);
	}
	
	//helper methods
	
		private WebTestClient.BodyContentSpec getAndVerifyCommentsByMealId(int mealId, HttpStatus expectedStatus) {
			return getAndVerifyCommentsByMealId("?mealId=" + mealId, expectedStatus);
		}

		private WebTestClient.BodyContentSpec getAndVerifyCommentsByMealId(String mealIdQuery, HttpStatus expectedStatus) {
			return client.get()
				.uri("/comment" + mealIdQuery)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
		}

	private void sendCreateCommentEvent(int mealId, int commentId) {
		Comment comment = new Comment(mealId, commentId, "Author " + commentId, "subj", "content", null, "SA");
		Event<Integer, Meal> event = new Event(CREATE, mealId, commentId);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteCommentEvent(int mealId) {
		Event<Integer, Meal> event = new Event(DELETE, mealId, null);
		input.send(new GenericMessage<>(event));
	}
}
