package se.magnus.api.core.meal;

public class Meal {
	private final int mealId;
	private final String mealName;
	private final String category;
	private final String reciepeDescription;
	private final double calories;
	private final String prepartionTime;
	private final int serves;
	private final String serviceAddress;

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
}
