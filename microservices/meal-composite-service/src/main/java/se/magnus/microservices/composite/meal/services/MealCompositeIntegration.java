package se.magnus.microservices.composite.meal.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.meal.MealService;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.ingredient.IngredientService;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.api.core.recommendeddrink.RecommendedDrinkService;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.comment.CommentService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import java.io.IOException;

import static reactor.core.publisher.Flux.empty;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;
@EnableBinding(MealCompositeIntegration.MessageSources.class)
@Component
public class MealCompositeIntegration
		implements MealService, IngredientService, CommentService, RecommendedDrinkService {

	private static final Logger LOG = LoggerFactory.getLogger(MealCompositeIntegration.class);

	private final WebClient webClient;
	private final ObjectMapper mapper;

	private final String mealServiceUrl;
	private final String recommendedDrinkServiceUrl;
	private final String ingredientServiceUrl;
	private final String commentServiceUrl;

	private MessageSources messageSources;
	public interface MessageSources {

		String OUTPUT_MEALS = "output-meals";
		String OUTPUT_RECOMMENDEDDRINKS = "output-recommendedDrinks";
		String OUTPUT_INGREDIENTS = "output-ingredients";
		String OUTPUT_COMMENTS = "output-comments";

		@Output(OUTPUT_MEALS)
		MessageChannel outputMeals();

		@Output(OUTPUT_RECOMMENDEDDRINKS)
		MessageChannel outputRecommendedDrinks();

		@Output(OUTPUT_INGREDIENTS)
		MessageChannel outputIngredients();

		@Output(OUTPUT_COMMENTS)
		MessageChannel outputComments();
	}

	@Autowired
	public MealCompositeIntegration(
			WebClient.Builder webClient,
			ObjectMapper mapper,
			MessageSources messageSources,

			@Value("${app.meal-service.host}") String mealServiceHost,
			@Value("${app.meal-service.port}") int mealServicePort,

			@Value("${app.recommended-drink-service.host}") String recommendedDrinkServiceHost,
			@Value("${app.recommended-drink-service.port}") int recommendedDrinkServicePort,

			@Value("${app.ingredient-service.host}") String ingredientServiceHost,
			@Value("${app.ingredient-service.port}") int ingredientServicePort,

			@Value("${app.comment-service.host}") String commentServiceHost,
			@Value("${app.comment-service.port}") int commentServicePort
			
			) {

		this.webClient = webClient.build();
		this.mapper = mapper;
		this.messageSources = messageSources;

		mealServiceUrl = "http://" + mealServiceHost + ":" + mealServicePort;
		recommendedDrinkServiceUrl = "http://" + recommendedDrinkServiceHost + ":" + recommendedDrinkServicePort;
		commentServiceUrl = "http://" + commentServiceHost + ":" + commentServicePort;
		ingredientServiceUrl = "http://" + ingredientServiceHost + ":" + ingredientServicePort;
	}

	@Override
	public Mono<Meal> getMeal(int mealId) {

		String url = mealServiceUrl + "/meal/" + mealId;
		LOG.debug("Will call getMeal API on URL: {}", url);

		return webClient.get().uri(url).retrieve()
				.bodyToMono(Meal.class).log().onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
		
	}

	@Override
	public Meal createMeal(Meal body) {
		messageSources.outputMeals().send(MessageBuilder.withPayload(
				new Event(CREATE, body.getMealId(), body)
		).build());
		return  body;
	}


	@Override
	public void deleteMeal(int mealId) {
		messageSources.outputMeals().send(MessageBuilder.withPayload(new Event(DELETE, mealId, null)).build());
	}

	@Override
	public Flux<RecommendedDrink> getRecommendedDrinks(int mealId) {

		String url = recommendedDrinkServiceUrl + "/recommendedDrink?mealId=" +  mealId;

		LOG.debug("Will call getRecommendedDrink API on URL: {}", url);

		// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
		return webClient.get().uri(url).retrieve()
				.bodyToFlux(RecommendedDrink.class).log().onErrorResume(error -> empty());
	}


	@Override
	public RecommendedDrink createRecommendedDrink(RecommendedDrink body) {
		messageSources.outputRecommendedDrinks().send(MessageBuilder.withPayload(
				new Event(CREATE, body.getMealId(), body)).build());
		return body;
	}

	@Override
	public void deleteRecommendedDrinks(int mealId) {
		messageSources.outputRecommendedDrinks().send(MessageBuilder.withPayload(
				new Event(DELETE, mealId, null)).build());
	}

	@Override
	public Flux<Comment> getComments(int mealId) {

		String url = commentServiceUrl + "/comment?mealId=" + mealId;
		LOG.debug("Will call getComments API on URL: {}", url);
		// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
		return webClient.get().uri(url).retrieve()
				.bodyToFlux(Comment.class).log().onErrorResume(error -> empty());
	}

	@Override
	public Comment createComment(Comment body) {
		messageSources.outputComments().send(MessageBuilder.withPayload(
				new Event(CREATE, body.getMealId(), body)).build());
		return body;
	}

	@Override
	public void deleteComments(int mealId) {
		messageSources.outputComments().send(MessageBuilder.withPayload(
				new Event(DELETE, mealId, null)).build());
	}

	@Override
	public Flux<Ingredient> getIngredients(int mealId) {

		String url = ingredientServiceUrl + "/ingredient?mealId=" + mealId;
		LOG.debug("Will call getIngredients API on URL: {}", url);
		// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
		return webClient.get().uri(url).retrieve()
				.bodyToFlux(Ingredient.class).log().onErrorResume(error -> empty());
	}

	@Override
	public Ingredient createIngredient(Ingredient body) {
		messageSources.outputIngredients().send(MessageBuilder.withPayload(
				new Event(CREATE, body.getMealId(), body)).build());
		return body;
	}

	@Override
	public void deleteIngredients(int mealId) {
		messageSources.outputIngredients().send(MessageBuilder.withPayload(
				new Event(DELETE, mealId, null)).build());
	}

	public Mono<Health> getMealHealth() {
		return getHealth(mealServiceUrl);
	}

	public Mono<Health> getRecommendedDrinkHealth() {
		return getHealth(recommendedDrinkServiceUrl);
	}

	public Mono<Health> getIngredientHealth() {
		return getHealth(ingredientServiceUrl);
	}

	public Mono<Health> getCommentHealth() {
		return getHealth(commentServiceUrl);
	}

	private Mono<Health> getHealth(String url) {
		url += "/actuator/health";
		LOG.debug("Will call the Health API on URL: {}", url);
		return webClient.get().uri(url).retrieve().bodyToMono(String.class)
				.map(s -> new Health.Builder().up().build())
				.onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
				.log();
	}

	private Throwable handleException(Throwable ex) {

		if (!(ex instanceof WebClientResponseException)) {
			LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
			return ex;
		}

		WebClientResponseException wcre = (WebClientResponseException)ex;

		switch (wcre.getStatusCode()) {

			case NOT_FOUND:
				return new NotFoundException(getErrorMessage(wcre));

			case UNPROCESSABLE_ENTITY :
				return new InvalidInputException(getErrorMessage(wcre));

			default:
				LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
				LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
				return ex;
		}
	}


	private String getErrorMessage(WebClientResponseException ex) {
		try {
			return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
		} catch (IOException ioex) {
			return ex.getMessage();
		}
	}

}
