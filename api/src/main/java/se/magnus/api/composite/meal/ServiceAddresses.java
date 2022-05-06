package se.magnus.api.composite.meal;

public class ServiceAddresses {
    private final String cmp;
    private final String mea;
    private final String ing;
    private final String com;
    private final String rec;

    public ServiceAddresses() {
        cmp = null;
        mea = null;
        ing = null;
        com = null;
        rec = null;
    }

    public ServiceAddresses(String compositeAddress, String mealAddress, String ingredientAddress, String commentAddress, String recommendedDrinkAddress) {
        this.cmp = compositeAddress;
        this.mea = mealAddress;
        this.ing = ingredientAddress;
        this.com = commentAddress;
        this.rec = recommendedDrinkAddress;
    }

    public String getCmp() {
        return cmp;
    }

    public String getMea() {
        return mea;
    }

    public String getIng() {
        return ing;
    }

    public String getCom() {
        return com;
    }
    
    public String getRec() {
        return rec;
    }
}