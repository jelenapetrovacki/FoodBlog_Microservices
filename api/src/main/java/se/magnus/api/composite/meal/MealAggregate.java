package se.magnus.api.composite.meal;

import java.util.List;

public class MealAggregate {
    private final int mealId;
    private final String mealName;
    private final String category;
    private final List<IngredientSummary> ingredients;
    private final List<CommentSummary> comments;
    private final List<RecommendedDrinkSummary> recommendedDrinks;
    private final ServiceAddresses serviceAddresses;

    public MealAggregate(
    		int mealId, String mealName, String category, List<IngredientSummary> ingredients, 
    		List<CommentSummary> comments, List<RecommendedDrinkSummary> recommendedDrinks, ServiceAddresses serviceAddresses) {

		this.mealId = mealId;
		this.mealName = mealName;
		this.category = category;
	    this.ingredients = ingredients;
	    this.comments = comments;
	    this.recommendedDrinks = recommendedDrinks;
		this.serviceAddresses = serviceAddresses;
    }

    public int getMealId() {
		return mealId;
	}

	public String getMealName() {
		return mealName;
	}
	
	public String getCategory() {
		return category;
	}

    public List<IngredientSummary> getIngredients() {
        return ingredients;
    }

    public List<CommentSummary> getComments() {
        return comments;
    }
    
    public List<RecommendedDrinkSummary> getRecommendedDrinks() {
        return recommendedDrinks;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }
}