package se.magnus.api.composite.meal;

import java.util.List;

public class MealAggregate {
    private final int mealId;
    private final String mealName;
    private final String category;
    private final String reciepeDescription;
	private final double calories;
	private final String prepartionTime;
	private final int serves;
    private final List<IngredientSummary> ingredients;
    private final List<CommentSummary> comments;
    private final List<RecommendedDrinkSummary> recommendedDrinks;
    private final ServiceAddresses serviceAddresses;

    public MealAggregate(
    		int mealId, String mealName, String category, String reciepeDescription, double calories, String prepartionTime, int serves,
    		List<IngredientSummary> ingredients, 
    		List<CommentSummary> comments, List<RecommendedDrinkSummary> recommendedDrinks, ServiceAddresses serviceAddresses) {

		this.mealId = mealId;
		this.mealName = mealName;
		this.category = category;
		this.reciepeDescription = reciepeDescription; 
		this.calories = calories;
		this.prepartionTime = prepartionTime;
		this.serves = serves;
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

	public String getReciepeDescription() {
		return reciepeDescription;
	}

	public double getCalories() {
		return calories;
	}

	public String getPrepartionTime() {
		return prepartionTime;
	}

	public int getServes() {
		return serves;
	}
}