package se.magnus.microservices.composite.meal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import se.magnus.api.composite.meal.CommentSummary;
import se.magnus.api.composite.meal.IngredientSummary;
import se.magnus.api.composite.meal.MealAggregate;
import se.magnus.api.composite.meal.MealCompositeService;
import se.magnus.api.composite.meal.RecommendedDrinkSummary;
import se.magnus.api.composite.meal.ServiceAddresses;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class MealCompositeServiceImpl implements MealCompositeService {

	private static final Logger LOG = LoggerFactory.getLogger(MealCompositeServiceImpl.class);

	private final ServiceUtil serviceUtil;
	private MealCompositeIntegration integration;

	@Autowired
	public MealCompositeServiceImpl(ServiceUtil serviceUtil, MealCompositeIntegration integration) {
		this.serviceUtil = serviceUtil;
		this.integration = integration;
	}

	@Override
	public MealAggregate getMeal(int mealId) {
		Meal meal = integration.getMeal(mealId);
		if (meal == null)
			throw new NotFoundException("No meal found for mealId: " + mealId);

		List<Comment> comments = integration.getComments(mealId);

		List<RecommendedDrink> recommendedDrinks = integration.getRecommendedDrinks(mealId);

		List<Ingredient> ingredients = integration.getIngredients(mealId);

		LOG.debug("getCompositeMeal: aggregate entity found for mealId: {}", mealId);

		return createMealAggregate(meal, comments, recommendedDrinks, ingredients, serviceUtil.getServiceAddress());
	}

	@Override
	public void createCompositeMeal(MealAggregate body) {
		try {

			LOG.debug("createCompositeMeal: creates a new composite entity for mealId: {}", body.getMealId());

			Meal meal = new Meal(body.getMealId(), body.getMealName(), body.getCategory(), body.getReciepeDescription(),
					body.getCalories(), body.getPrepartionTime(), body.getServes(), null);
			integration.createMeal(meal);

			if (body.getIngredients() != null) {
				body.getIngredients().forEach(r -> {
					Ingredient ingredient = new Ingredient(body.getMealId(), r.getIngredientId(), r.getName(),
							(int) r.getAmount(), r.getUnitOfMeasure(), null);
					integration.createIngredient(ingredient);
				});
			}

			if (body.getComments() != null) {
				body.getComments().forEach(r -> {
					Comment comment = new Comment(body.getMealId(), r.getCommentId(), r.getAuthor(), r.getSubject(),
							null, null, null);
					integration.createComment(comment);
				});
			}

			if (body.getRecommendedDrinks() != null) {
				body.getRecommendedDrinks().forEach(r -> {
					RecommendedDrink recommendedDrink = new RecommendedDrink(body.getMealId(),
							r.getRecommendedDrinkId(), r.getDrinkName(), null, false, null, null, null);
					integration.createRecommendedDrink(recommendedDrink);
				});
			}

			LOG.debug("createCompositeMeal: composite entites created for mealId: {}", body.getMealId());

		} catch (RuntimeException re) {
			LOG.warn("createCompositeProduct failed", re);
			throw re;
		}

	}

	@Override
	public void deleteCompositeMeal(int mealId) {
		LOG.debug("deleteCompositeMeal: Deletes a meal aggregate for mealId: {}", mealId);

		integration.deleteMeal(mealId);

		integration.deleteIngredients(mealId);

		integration.deleteComments(mealId);
		
		integration.deleteRecommendedDrinks(mealId);

		LOG.debug("getCompositeMeal: aggregate entities deleted for mealId: {}", mealId);

	}

	private MealAggregate createMealAggregate(Meal meal, List<Comment> comments,
			List<RecommendedDrink> recommendedDrinks, List<Ingredient> ingredients, String serviceAddress) {

		// 1. Setup meal info
		int mealId = meal.getMealId();
		String name = meal.getMealName();
		String category = meal.getCategory();
		String recipeDescription = meal.getReciepeDescription();
		double calories = meal.getCalories();
		String preparationTime = meal.getPrepartionTime();
		int serves = meal.getServes();

		// 2. Copy summary recommendedDrinks info, if available
		List<RecommendedDrinkSummary> recommendedDrinksSummaries = (recommendedDrinks == null) ? null
				: recommendedDrinks.stream().map(r -> new RecommendedDrinkSummary(r.getRecommendedDrinkId(),
						r.getDrinkName(), r.isNonalcoholic())).collect(Collectors.toList());

		// 3. Copy summary comments info, if available
		List<CommentSummary> commentsSummaries = (comments == null) ? null
				: comments.stream().map(r -> new CommentSummary(r.getCommentId(), r.getAuthor(), r.getSubject()))
						.collect(Collectors.toList());

		// 4. Copy summary ingredients info, if available
		List<IngredientSummary> ingredientsSummaries = (ingredients == null) ? null
				: ingredients.stream().map(r -> new IngredientSummary(r.getIngredientId(), r.getName(),
						(int) r.getAmount(), r.getUnitOfMeasure())).collect(Collectors.toList());

		// 4. Create info regarding the involved microservices addresses
		String mealAddress = meal.getServiceAddress();
		String recommendedDrinkAddress = (recommendedDrinks != null && recommendedDrinks.size() > 0)
				? recommendedDrinks.get(0).getServiceAddress()
				: "";
		String commentAddress = (comments != null && comments.size() > 0) ? comments.get(0).getServiceAddress() : "";
		String ingredientAddress = (ingredients != null && ingredients.size() > 0)
				? ingredients.get(0).getServiceAddress()
				: "";
		ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, mealAddress, ingredientAddress,
				commentAddress, recommendedDrinkAddress);

		return new MealAggregate(mealId, name, category, recipeDescription, calories, preparationTime, serves,
				ingredientsSummaries, commentsSummaries, recommendedDrinksSummaries, serviceAddresses);
	}

}
