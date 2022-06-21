package se.magnus.microservices.core.recommendeddrink.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="recommendeddrinks")
@CompoundIndex(name = "meal-rec-id", unique = true, def = "{'mealId': 1, 'recommendedDrinkId' : 1}")
public class RecommendedDrinkEntity {

	@Id
	private String id;

	@Version
	private Integer version;
	
	private int mealId;
	private int recommendedDrinkId;
	private String drinkName;
	private String drinkType;
	private boolean nonalcoholic;
	private String glassType;
	private String drinkBrand;
	
	public RecommendedDrinkEntity() {
	}
	
	public RecommendedDrinkEntity(int mealId, int recommendedDrinkId, String drinkName,
			String drinkType, boolean nonalcoholic, String glassType, String drinkBrand) {
		this.mealId = mealId;
		this.recommendedDrinkId = recommendedDrinkId;
		this.drinkName = drinkName;
		this.drinkType = drinkType;
		this.nonalcoholic = nonalcoholic;
		this.glassType = glassType;
		this.drinkBrand = drinkBrand;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public int getMealId() {
		return mealId;
	}

	public void setMealId(int mealId) {
		this.mealId = mealId;
	}

	public int getRecommendedDrinkId() {
		return recommendedDrinkId;
	}

	public void setRecommendedDrinkId(int recommendedDrinkId) {
		this.recommendedDrinkId = recommendedDrinkId;
	}

	public String getDrinkName() {
		return drinkName;
	}

	public void setDrinkName(String drinkName) {
		this.drinkName = drinkName;
	}

	public String getDrinkType() {
		return drinkType;
	}

	public void setDrinkType(String drinkType) {
		this.drinkType = drinkType;
	}

	public boolean isNonalcoholic() {
		return nonalcoholic;
	}

	public void setNonalcoholic(boolean nonalcoholic) {
		this.nonalcoholic = nonalcoholic;
	}

	public String getGlassType() {
		return glassType;
	}

	public void setGlassType(String glassType) {
		this.glassType = glassType;
	}

	public String getDrinkBrand() {
		return drinkBrand;
	}

	public void setDrinkBrand(String drinkBrand) {
		this.drinkBrand = drinkBrand;
	}
	
}
