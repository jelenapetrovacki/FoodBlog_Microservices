
package se.magnus.api.core.meal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}