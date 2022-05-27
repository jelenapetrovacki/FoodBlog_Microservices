package se.magnus.api.core.ingredient;

public class Ingredient {
    private int mealId;
    private int ingredientId;
    private String name;
    private double amount;
    private String unitOfMeasure;
    private String serviceAddress;
    
    public Ingredient() {
    	mealId = 0;
    	ingredientId = 0;
        name = null;
        amount = 0;
        unitOfMeasure = null;
        serviceAddress = null;
    }

    public Ingredient(int mealId, int ingredientId, String name, int amount, String unitOfMeasure, String serviceAddress) {
        this.mealId = mealId;
        this.ingredientId = ingredientId;
        this.name = name;
        this.amount = amount;
        this.unitOfMeasure = unitOfMeasure;
        this.serviceAddress = serviceAddress;
    }
    
    public int getMealId() {
        return mealId;
    }
    
    public int getIngredientId() {
        return ingredientId;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }
    
    public String getServiceAddress() {
        return serviceAddress;
    }

	public void setMealId(int mealId) {
		this.mealId = mealId;
	}

	public void setIngredientId(int ingredientId) {
		this.ingredientId = ingredientId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
}