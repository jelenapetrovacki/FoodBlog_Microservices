package se.magnus.microservices.composite.meal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import reactor.core.publisher.Mono;
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
import se.magnus.util.http.ServiceUtil;

@SuppressWarnings("unchecked")
@RestController
public class MealCompositeServiceImpl implements MealCompositeService {

	private static final Logger LOG = LoggerFactory.getLogger(MealCompositeServiceImpl.class);

	private final SecurityContext nullSC = new SecurityContextImpl();
	private final ServiceUtil serviceUtil;
	private MealCompositeIntegration integration;

	@Autowired
	public MealCompositeServiceImpl(ServiceUtil serviceUtil, MealCompositeIntegration integration) {
		this.serviceUtil = serviceUtil;
		this.integration = integration;
	}

	@Override
	public Mono<MealAggregate> getCompositeMeal(int mealId) {
		return Mono.zip(
						values -> createMealAggregate((SecurityContext) values[0], (Meal) values[1], (List<Comment>) values[2], (List<RecommendedDrink>) values[3],(List<Ingredient>) values[4], serviceUtil.getServiceAddress()),
											ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
						integration.getMeal(mealId),
						integration.getComments(mealId).collectList(),
						integration.getRecommendedDrinks(mealId).collectList(),
						integration.getIngredients(mealId).collectList())
				.doOnError(ex -> LOG.warn("getCompositeMeal failed: {}", ex.toString()))
				.log();
	}
	@Override
	public Mono<Void> createCompositeMeal(MealAggregate body) {
		return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc
				-> internalCreateCompositeMeal(sc, body)).then();

	}
	public void internalCreateCompositeMeal(SecurityContext sc, MealAggregate body) {


		try {
			logAuthorizationInfo(sc);
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
	public Mono<Void> deleteCompositeMeal(int mealId) {

		return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalDeleteCompositeMeal(sc, mealId)).then();

	}

	private void internalDeleteCompositeMeal(SecurityContext sc, int mealId) {
		try {
			logAuthorizationInfo(sc);
			LOG.debug("deleteCompositeMeal: Deletes a meal aggregate for mealId: {}", mealId);

			integration.deleteMeal(mealId);

			integration.deleteIngredients(mealId);

			integration.deleteComments(mealId);

			integration.deleteRecommendedDrinks(mealId);

			LOG.debug("getCompositeMeal: aggregate entities deleted for mealId: {}", mealId);
		} catch (RuntimeException re) {
			LOG.warn("deleteCompositeMeal failed: {}", re.toString());
			throw re;
		}
	}

	private MealAggregate createMealAggregate(SecurityContext sc, Meal meal, List<Comment> comments,
			List<RecommendedDrink> recommendedDrinks, List<Ingredient> ingredients, String serviceAddress) {

		logAuthorizationInfo(sc);

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

	private void logAuthorizationInfo(SecurityContext sc) {
		if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
			Jwt jwtToken = ((JwtAuthenticationToken)sc.getAuthentication()).getToken();
			logAuthorizationInfo(jwtToken);
		} else {
			LOG.warn("No JWT based Authentication supplied, running tests are we?");
		}
	}

	private void logAuthorizationInfo(Jwt jwt) {
		if (jwt == null) {
			LOG.warn("No JWT supplied, running tests are we?");
		} else {
			if (LOG.isDebugEnabled()) {
				URL issuer = jwt.getIssuer();
				List<String> audience = jwt.getAudience();
				Object subject = jwt.getClaims().get("sub");
				Object scopes = jwt.getClaims().get("scope");
				Object expires = jwt.getClaims().get("exp");

				LOG.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject, scopes, expires, issuer, audience);
			}
		}
	}

}
