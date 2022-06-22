package se.magnus.api.core.ingredient;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

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
    Flux<Ingredient> getIngredients(@RequestParam(value = "mealId", required = true) int mealId);
    
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/ingredient \
     *   -H "Content-Type: application/json" --data \
     *   '{"mealId":123,"ingredientId":456,"name":"ing","amount":5,"unitOfMeasure":"g"}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/ingredient",
        consumes = "application/json",
        produces = "application/json")
    Ingredient createIngredient(@RequestBody Ingredient body);
    
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/ingredient?mealId=1
     *
     * @param mealId
     */
    @DeleteMapping(value = "/ingredient")
    void deleteIngredients(@RequestParam(value = "mealId", required = true)  int mealId);
}