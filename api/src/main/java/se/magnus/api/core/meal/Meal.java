package se.magnus.api.core.meal;

public class Meal {
	private int mealId;
	private String mealName;
	private String category;
	private String reciepeDescription;
	private double calories;
	private String prepartionTime;
	private int serves;
	private String serviceAddress;

	
	public Meal() {
		mealId = 0;
		mealName = null;
		category = null;
		reciepeDescription = null;
		calories = 0;
		prepartionTime = null;
		serves = 0;
		serviceAddress = null;
	}

	public Meal(int mealId, String mealName, String category, String reciepeDescription, double calories,
			String prepartionTime, int serves, String serviceAddress) {
		this.mealId = mealId;
		this.mealName = mealName;
		this.category = category;
		this.reciepeDescription = reciepeDescription;
		this.calories = calories;
		this.prepartionTime = prepartionTime;
		this.serves = serves;
		this.serviceAddress = serviceAddress;
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
	
	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setMealId(int mealId) {
		this.mealId = mealId;
	}

	public void setMealName(String mealName) {
		this.mealName = mealName;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setReciepeDescription(String reciepeDescription) {
		this.reciepeDescription = reciepeDescription;
	}

	public void setCalories(double calories) {
		this.calories = calories;
	}

	public void setPrepartionTime(String prepartionTime) {
		this.prepartionTime = prepartionTime;
	}

	public void setServes(int serves) {
		this.serves = serves;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

}
