package se.magnus.api.core.ingredient;

public class Ingredient {
    private final int mealId;
    private final int ingredientId;
    private final String name;
    private final double amount;
    private final String unitOfMeasure;
    private final String serviceAddress;
    
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
}