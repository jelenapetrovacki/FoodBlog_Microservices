
package se.magnus.api.core.meal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface MealService {

    /**
     * Sample usage: curl $HOST:$PORT/meal/1
     *
     * @param mealId
     * @return the meal, if found, else null
     */
    @GetMapping(
        value    = "/meal/{mealId}",
        produces = "application/json")
     Meal getMeal(@PathVariable int mealId);
    
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/meal \
     *   -H "Content-Type: application/json" --data \
     *   '{"mealId":123,"mealName":"meal 123","category":"cat", "reciepeDescription":"desc", "calories":100, "preparationTime":"1h", "serves":2}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/meal",
        consumes = "application/json",
        produces = "application/json")
    Meal createMeal(@RequestBody Meal body);
    
    
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/meal/1
     *
     * @param mealId
     */
    @DeleteMapping(value = "/meal/{mealId}")
    void deleteMeal(@PathVariable int mealId);
}