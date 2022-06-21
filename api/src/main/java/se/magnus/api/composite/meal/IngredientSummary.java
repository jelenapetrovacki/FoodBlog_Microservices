package se.magnus.api.composite.meal;

public class IngredientSummary {
    private final int ingredientId;
    private final String name;
    private final double amount;
    private final String unitOfMeasure;

    public IngredientSummary () {
    	 this.ingredientId = 0;
         this.name = null;
         this.amount = 0;
         this.unitOfMeasure = null;
    }
    public IngredientSummary(int ingredientId, String name, int amount, String unitOfMeasure) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.amount = amount;
        this.unitOfMeasure = unitOfMeasure;
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
}