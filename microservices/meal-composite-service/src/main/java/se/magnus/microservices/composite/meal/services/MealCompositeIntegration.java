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

@Component
public class MealCompositeIntegration
		implements MealService, IngredientService, CommentService, RecommendedDrinkService {

	private static final Logger LOG = LoggerFactory.getLogger(MealCompositeIntegration.class);
	private final RestTemplate restTemplate;
	private final ObjectMapper mapper;

	private final String mealServiceUrl;
	private final String recommendedDrinkServiceUrl;
	private final String ingredientServiceUrl;
	private final String commentServiceUrl;

	@Autowired
	public MealCompositeIntegration(RestTemplate restTemplate, ObjectMapper mapper,

			@Value("${app.meal-service.host}") String mealServiceHost,
			@Value("${app.meal-service.port}") int mealServicePort,

			@Value("${app.recommended-drink-service.host}") String recommendedDrinkServiceHost,
			@Value("${app.recommended-drink-service.port}") int recommendedDrinkServicePort,

			@Value("${app.ingredient-service.host}") String ingredientServiceHost,
			@Value("${app.ingredient-service.port}") int ingredientServicePort,

			@Value("${app.comment-service.host}") String commentServiceHost,
			@Value("${app.comment-service.port}") int commentServicePort
			
			) {

		this.restTemplate = restTemplate;
		this.mapper = mapper;

		mealServiceUrl = "http://" + mealServiceHost + ":" + mealServicePort + "/meal";
		recommendedDrinkServiceUrl = "http://" + recommendedDrinkServiceHost + ":" + recommendedDrinkServicePort
				+ "/recommendedDrink";
		commentServiceUrl = "http://" + commentServiceHost + ":" + commentServicePort + "/comment";
		ingredientServiceUrl = "http://" + ingredientServiceHost + ":" + ingredientServicePort + "/ingredient";
	}

	@Override
	public Meal getMeal(int mealId) {
		try {
			String url = mealServiceUrl + "/" + mealId;
			LOG.debug("Will call getMeal API on URL: {}", url);

			Meal meal = restTemplate.getForObject(url, Meal.class);
			LOG.debug("Found a meal with id: {}", meal.getMealId());

			return meal;

		} catch (HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
		
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
	public List<RecommendedDrink> getRecommendedDrinks(int mealId) {
		try {
			String url = recommendedDrinkServiceUrl + "?mealId=" +  mealId;

			LOG.debug("Will call getRecommendedDrink API on URL: {}", url);
			List<RecommendedDrink> recommendedDrinks = restTemplate
					.exchange(url, GET, null, new ParameterizedTypeReference<List<RecommendedDrink>>() {
					}).getBody();

			LOG.debug("Found {} recommendedDrinks for a meal with id: {}", recommendedDrinks.size(), mealId);
			return recommendedDrinks;

		} catch (Exception ex) {
			LOG.warn("Got an exception while requesting recommended drinks, return zero recommended drinks: {}",
					ex.getMessage());
			return new ArrayList<>();
		}
		
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
	public List<Comment> getComments(int mealId) {
		try {
			String url = commentServiceUrl + "?mealId=" + mealId;

			LOG.debug("Will call getComments API on URL: {}", url);
			List<Comment> comments = restTemplate
					.exchange(url, GET, null, new ParameterizedTypeReference<List<Comment>>() {
					}).getBody();

			LOG.debug("Found {} comments for a meal with id: {}", comments.size(), mealId);
			return comments;

		} catch (Exception ex) {
			LOG.warn("Got an exception while requesting comments, return zero comments: {}", ex.getMessage());
			return new ArrayList<>();
		}
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
	public List<Ingredient> getIngredients(int mealId) {
		try {
			String url = ingredientServiceUrl + "?mealId=" + mealId;

			LOG.debug("Will call getIngredients API on URL: {}", url);
			List<Ingredient> ingredients = restTemplate
					.exchange(url, GET, null, new ParameterizedTypeReference<List<Ingredient>>() {
					}).getBody();

			LOG.debug("Found {} ingredients for a meal with id: {}", ingredients.size(), mealId);
			return ingredients;

		} catch (Exception ex) {
			LOG.warn("Got an exception while requesting ingredients, return zero ingredients: {}", ex.getMessage());
			return new ArrayList<>();
		}
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
    

	private String getErrorMessage(HttpClientErrorException ex) {
		try {
			return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
		} catch (IOException ioex) {
			return ex.getMessage();
		}
	}

}
