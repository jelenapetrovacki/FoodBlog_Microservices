package se.magnus.api.core.recommendeddrink;

public class RecommendedDrink {
	private final int mealId;
	private final int recommendedDrinkId;
	private final String drinkName;
	private final String drinkType;
	private final boolean nonalcoholic;
	private final String glassType;
	private final String drinkBrand;
	private final String serviceAddress;

	public RecommendedDrink() {
    	mealId = 0;
    	recommendedDrinkId = 0;
    	drinkName = null;
    	drinkType = null;
    	nonalcoholic = false;
    	glassType = null;
    	drinkBrand = null;
    	serviceAddress = null;
    }

	public RecommendedDrink(int mealId, int recommendedDrinkId, String drinkName, String drinkType, boolean nonalcoholic,
			String glassType, String drinkBrand, String serviceAddress) {
    	this.mealId = mealId;
    	this.recommendedDrinkId = recommendedDrinkId;
    	this.drinkName = drinkName;
    	this.drinkType = drinkType;
    	this.nonalcoholic = nonalcoholic;
    	this.glassType = glassType;
    	this.drinkBrand = drinkBrand;
    	this.serviceAddress = serviceAddress;
    }
	
	public int getMealId() {
		return mealId;
	}

	public int getRecommendedDrinkId() {
		return recommendedDrinkId;
	}
	
	public String getDrinkName() {
		return drinkName;
	}
	
	public String getDrinkType() {
		return drinkType;
	}
	
	public boolean isNonalcoholic() {
		return nonalcoholic;
	}
	
	public String getGlassType() {
		return glassType;
	}
	
	public String getDrinkBrand() {
		return drinkBrand;
	}
	
	public String getServiceAddress() {
		return serviceAddress;
	}
}