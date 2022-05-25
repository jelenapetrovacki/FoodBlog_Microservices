package se.magnus.microservices.core.meal.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "meals")
public class MealEntity {

	@Id
	private String id;

	@Version
	private Integer version;

	@Indexed(unique = true)
	private int mealId;

	private String mealName;
	private String category;
	private String reciepeDescription;
	private double calories;
	private String prepartionTime;
	private int serves;
	
	public MealEntity() {
	}

	public MealEntity(int mealId, String mealName, String category,
			String reciepeDescription, double calories, String prepartionTime, int serves) {
		this.mealId = mealId;
		this.mealName = mealName;
		this.category = category;
		this.reciepeDescription = reciepeDescription;
		this.calories = calories;
		this.prepartionTime = prepartionTime;
		this.serves = serves;
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

	public String getMealName() {
		return mealName;
	}

	public void setMealName(String mealName) {
		this.mealName = mealName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getReciepeDescription() {
		return reciepeDescription;
	}

	public void setReciepeDescription(String reciepeDescription) {
		this.reciepeDescription = reciepeDescription;
	}

	public double getCalories() {
		return calories;
	}

	public void setCalories(double calories) {
		this.calories = calories;
	}

	public String getPrepartionTime() {
		return prepartionTime;
	}

	public void setPrepartionTime(String prepartionTime) {
		this.prepartionTime = prepartionTime;
	}

	public int getServes() {
		return serves;
	}

	public void setServes(int serves) {
		this.serves = serves;
	}

}
