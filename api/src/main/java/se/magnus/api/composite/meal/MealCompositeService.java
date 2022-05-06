
package se.magnus.api.composite.meal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface MealCompositeService {

    /**
     * Sample usage: curl $HOST:$PORT/meal-composite/1
     *
     * @param mealId
     * @return the composite meal info, if found, else null
     */
    @GetMapping(
        value    = "/meal-composite/{mealId}",
        produces = "application/json")
    MealAggregate getMeal(@PathVariable int mealId);
}