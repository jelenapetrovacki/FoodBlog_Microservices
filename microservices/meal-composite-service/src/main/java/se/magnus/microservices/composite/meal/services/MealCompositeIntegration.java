package se.magnus.microservices.composite.meal.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
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
import java.net.URI;
import java.time.Duration;
import static reactor.core.publisher.Flux.empty;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;
@EnableBinding(MealCompositeIntegration.MessageSources.class)
@Component
public class MealCompositeIntegration
		implements MealService, IngredientService, CommentService, RecommendedDrinkService {

	private static final Logger LOG = LoggerFactory.getLogger(MealCompositeIntegration.class);

	private final String mealServiceUrl = "http://meal";
	private final String recommendedDrinkServiceUrl = "http://recommendedDrink";
	private final String ingredientServiceUrl = "http://ingredient";
	private final String commentServiceUrl = "http://comment";

	private final WebClient.Builder webClientBuilder;
	private WebClient webClient;
	private final ObjectMapper mapper;

	private final MessageSources messageSources;
	private final int mealServiceTimeoutSec;

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
			WebClient.Builder webClientBuilder,
			ObjectMapper mapper,
			MessageSources messageSources,
			@Value("${app.meal-service.timeoutSec}") int mealServiceTimeoutSec
			) {

		this.webClientBuilder = webClientBuilder;
		this.mapper = mapper;
		this.messageSources = messageSources;
		this.mealServiceTimeoutSec = mealServiceTimeoutSec;

	}
	@Retry(name = "meal")
	@CircuitBreaker(name = "meal")
	@Override
	public Mono<Meal> getMeal(int mealId, int delay, int faultPercent) {
		URI url = UriComponentsBuilder.fromUriString(mealServiceUrl + "/meal/{mealId}?delay={delay}&faultPercent={faultPercent}").build(mealId, delay, faultPercent);

		LOG.debug("Will call getMeal API on URL: {}", url);

		return getWebClient().get().uri(url).retrieve()
				.bodyToMono(Meal.class).log().onErrorMap(WebClientResponseException.class, ex -> handleException(ex)).timeout(Duration.ofSeconds(mealServiceTimeoutSec));
		
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

		URI url = UriComponentsBuilder.fromUriString(recommendedDrinkServiceUrl + "/recommendedDrink?mealId={mealId}").build(mealId);
		LOG.debug("Will call getRecommendedDrink API on URL: {}", url);

		// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
		return getWebClient().get().uri(url).retrieve()
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
		URI url = UriComponentsBuilder.fromUriString(commentServiceUrl + "/comment?mealId={mealId}").build(mealId);
		LOG.debug("Will call getComments API on URL: {}", url);
		// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
		return getWebClient().get().uri(url).retrieve()
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
		URI url = UriComponentsBuilder.fromUriString(ingredientServiceUrl + "/ingredient?mealId={mealId}").build(mealId);
		LOG.debug("Will call getIngredients API on URL: {}", url);
		// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
		return getWebClient().get().uri(url).retrieve()
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



	private WebClient getWebClient() {
		if (webClient == null) {
			webClient = webClientBuilder.build();
		}
		return webClient;
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
