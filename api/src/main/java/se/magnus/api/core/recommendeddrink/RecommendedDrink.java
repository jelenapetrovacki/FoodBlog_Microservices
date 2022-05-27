package se.magnus.api.core.recommendeddrink;

public class RecommendedDrink {
	private int mealId;
	private int recommendedDrinkId;
	private String drinkName;
	private String drinkType;
	private boolean nonalcoholic;
	private String glassType;
	private String drinkBrand;
	private String serviceAddress;

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

	public void setMealId(int mealId) {
		this.mealId = mealId;
	}

	public void setRecommendedDrinkId(int recommendedDrinkId) {
		this.recommendedDrinkId = recommendedDrinkId;
	}

	public void setDrinkName(String drinkName) {
		this.drinkName = drinkName;
	}

	public void setDrinkType(String drinkType) {
		this.drinkType = drinkType;
	}

	public void setNonalcoholic(boolean nonalcoholic) {
		this.nonalcoholic = nonalcoholic;
	}

	public void setGlassType(String glassType) {
		this.glassType = glassType;
	}

	public void setDrinkBrand(String drinkBrand) {
		this.drinkBrand = drinkBrand;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
}