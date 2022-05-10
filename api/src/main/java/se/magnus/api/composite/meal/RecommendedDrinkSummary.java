package se.magnus.api.composite.meal;

public class RecommendedDrinkSummary {
	private final int recommendedDrinkId;
	private final String drinkName;
	private final boolean nonalcoholic;

	public RecommendedDrinkSummary(int recommendedDrinkId, String drinkName, boolean nonalcoholic) {
    	this.recommendedDrinkId = recommendedDrinkId;
    	this.drinkName = drinkName;
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