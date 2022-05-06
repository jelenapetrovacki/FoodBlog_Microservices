package se.magnus.api.composite.meal;

public class RecommendedDrinkSummary {
	private final int recommendedDrinkId;
	private final String drinkName;
	private final boolean nonalcoholic;

	public RecommendedDrinkSummary(int mealId, int recommendedDrinkId, String drinkType, boolean nonalcoholic,
			String glassType, String drinkBrand, String serviceAddress) {
    	this.recommendedDrinkId = recommendedDrinkId;
    	this.drinkName = drinkType;
    	this.nonalcoholic = nonalcoholic;
    }

	public int getRecommendedDrinkId() {
		return recommendedDrinkId;
	}
	
	public String getDrinkName() {
		return drinkName;
	}
	
	public boolean isNonalcoholic() {
		return nonalcoholic;
	}
}