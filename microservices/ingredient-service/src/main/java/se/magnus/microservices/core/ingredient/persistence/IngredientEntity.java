package se.magnus.microservices.core.ingredient.persistence;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;

@Document(collection = "ingredients")
@CompoundIndex(name = "meal-ing-id", unique = true, def = "{'mealId': 1, 'ingredientId' : 1}")
public class IngredientEntity {

	@Id
	private String id;

	@Version
	private Integer version;
	
	private int mealId;
	private int ingredientId;
	private String name;
	private double amount;
	private String unitOfMeasure;
	
	public IngredientEntity() {
	}

	public IngredientEntity(String id, Integer version, int mealId, int ingredientId, String name, double amount,
			String unitOfMeasure) {
		this.id = id;
		this.version = version;
		this.mealId = mealId;
		this.ingredientId = ingredientId;
		this.name = name;
		this.amount = amount;
		this.unitOfMeasure = unitOfMeasure;
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

	public int getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(int ingredientId) {
		this.ingredientId = ingredientId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}
	
	
}
