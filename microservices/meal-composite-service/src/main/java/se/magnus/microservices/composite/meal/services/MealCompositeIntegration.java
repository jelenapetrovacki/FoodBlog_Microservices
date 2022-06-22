package se.magnus.microservices.composite.meal.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.comment.CommentService;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.ingredient.IngredientService;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.meal.MealService;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.api.core.recommendeddrink.RecommendedDrinkService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import static org.springframework.http.HttpMethod.GET;
import static reactor.core.publisher.Flux.empty;

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

	@Autowired
	public MealCompositeIntegration(WebClient.Builder webClient, RestTemplate restTemplate, ObjectMapper mapper,

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
		try {
			String url = mealServiceUrl;
			LOG.debug("Will post a new meal to URL: {}", url);

			Meal meal = restTemplate.postForObject(url, body, Meal.class);
			LOG.debug("Created a meal with id: {}", meal.getMealId());

			return meal;

		} catch (HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
	}

	@Override
	public void deleteMeal(int mealId) {
		try {
            String url = mealServiceUrl + "/" + mealId;
            LOG.debug("Will call the deleteMeal API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
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
		try {
            String url = recommendedDrinkServiceUrl;
            LOG.debug("Will post a new recommended drink to URL: {}", url);

            RecommendedDrink recommendedDrink = restTemplate.postForObject(url, body, RecommendedDrink.class);
            LOG.debug("Created a recommended drink with id: {}", recommendedDrink.getRecommendedDrinkId());

            return recommendedDrink;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
	}

	@Override
	public void deleteRecommendedDrinks(int mealId) {
		try {
            String url = recommendedDrinkServiceUrl + "?mealId=" + mealId;
            LOG.debug("Will call the deleteRecommendedDrinks() API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }

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
		try {
            String url = commentServiceUrl;
            LOG.debug("Will post a new comment to URL: {}", url);

            Comment comment = restTemplate.postForObject(url, body, Comment.class);
            LOG.debug("Created a comment with id: {}", comment.getCommentId());

            return comment;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
	}

	@Override
	public void deleteComments(int mealId) {
		try {
            String url = commentServiceUrl + "?mealId=" + mealId;
            LOG.debug("Will call the deleteComments() API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
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
		try {
            String url = ingredientServiceUrl;
            LOG.debug("Will post a new ingredient to URL: {}", url);

            Ingredient ingredient = restTemplate.postForObject(url, body, Ingredient.class);
            LOG.debug("Created a ingredient with id: {}", ingredient.getIngredientId());

            return ingredient;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
	}

	@Override
	public void deleteIngredients(int mealId) {
		try {
            String url = ingredientServiceUrl + "?mealId=" + mealId;
            LOG.debug("Will call the deleteIngredients() API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
	}
	
    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (ex.getStatusCode()) {

        case NOT_FOUND:
            return new NotFoundException(getErrorMessage(ex));

        case UNPROCESSABLE_ENTITY :
            return new InvalidInputException(getErrorMessage(ex));

        default:
            LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
            LOG.warn("Error body: {}", ex.getResponseBodyAsString());
            return ex;
        }
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


	private String getErrorMessage(HttpClientErrorException ex) {
		try {
			return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
		} catch (IOException ioex) {
			return ex.getMessage();
		}
	}

}
