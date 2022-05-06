package se.magnus.api.core.ingredient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface IngredientService {

    /**
     * Sample usage: curl $HOST:$PORT/ingredient?mealId=1
     *
     * @param mealId
     * @return
     */
    @GetMapping(
        value    = "/ingredient",
        produces = "application/json")
    List<Ingredient> getIngredients(@RequestParam(value = "mealId", required = true) int mealId);
}