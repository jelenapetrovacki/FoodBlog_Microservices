package se.magnus.api.core.meal;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    Mono<Meal> getMeal(@PathVariable int mealId,
                       @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
                       @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent);
    
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