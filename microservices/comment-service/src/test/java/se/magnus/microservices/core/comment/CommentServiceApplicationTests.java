package se.magnus.microservices.core.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.magnus.api.core.comment.Comment;
import se.magnus.microservices.core.comment.persistence.CommentRepository;

@SpringBootTest(webEnvironment=RANDOM_PORT,properties = {
"spring.datasource.url=jdbc:h2:mem:review-db"})
class CommentServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private CommentRepository repository; 
	
	@BeforeEach
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getCommentsByMealId() {

		int mealId = 1;

		assertEquals(0, repository.findByMealId(mealId).size());

		postAndVerifyComment(mealId, 1, OK);
		postAndVerifyComment(mealId, 2, OK);
		postAndVerifyComment(mealId, 3, OK);

		assertEquals(3, repository.findByMealId(mealId).size());

		getAndVerifyCommentsByMealId(mealId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].mealId").isEqualTo(mealId)
			.jsonPath("$[2].commentId").isEqualTo(3);
	}

/*	@Test
	public void duplicateError() {

		int mealId = 1;
		int commentId = 1;

		postAndVerifyComment(mealId, commentId, OK)
			.jsonPath("$.mealId").isEqualTo(mealId)
			.jsonPath("$.commentId").isEqualTo(commentId);

		assertEquals(1, repository.count());

		postAndVerifyComment(mealId, commentId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/comment")
			.jsonPath("$.message").isEqualTo("Duplicate key, Meal Id: 1, Comment Id:1");

		assertEquals(1, repository.count());
	}
*/	
	@Test
	public void deleteComments() {

		int mealId = 1;
		int commentId = 1;

		postAndVerifyComment(mealId, commentId, OK);
		assertEquals(1, repository.findByMealId(mealId).size());

		deleteAndVerifyCommentsByMealId(mealId, OK);
		assertEquals(0, repository.findByMealId(mealId).size());

		deleteAndVerifyCommentsByMealId(mealId, OK);
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

		private WebTestClient.BodyContentSpec postAndVerifyComment(int mealId, int commentId, HttpStatus expectedStatus) {
			//int mealId, int commentId, String author, String subject, String content, Date dateTime, String serviceAddress
			Comment comment = new Comment(mealId, commentId, "Author " + commentId, "subj", "content", null, "SA");
			return client.post()
				.uri("/comment")
				.body(just(comment), Comment.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
		}

		private WebTestClient.BodyContentSpec deleteAndVerifyCommentsByMealId(int mealId, HttpStatus expectedStatus) {
			return client.delete()
				.uri("/comment?mealId=" + mealId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
		}
}
