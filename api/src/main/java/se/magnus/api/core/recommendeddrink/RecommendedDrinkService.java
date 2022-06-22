package se.magnus.api.core.recommendeddrink;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

import java.util.List;

public interface RecommendedDrinkService {

    /**
     * Sample usage: curl $HOST:$PORT/recommendedDrink?mealId=1
     *
     * @param mealId
     * @return
     */
    @GetMapping(
        value    = "/recommendedDrink",
        produces = "application/json")
    Flux<RecommendedDrink> getRecommendedDrinks(@RequestParam(value = "mealId", required = true) int mealId);
    
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/recommendedDrink \
     *   -H "Content-Type: application/json" --data \
     *   '{"mealId":123,"recommendedDrinkId":456,"drinkName":"name","drinkType":"type","nonalcoholic":true, "glassType":"glass", "drinkBrand":"brand"}'
	 *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/recommendedDrink",
        consumes = "application/json",
        produces = "application/json")
    RecommendedDrink createRecommendedDrink(@RequestBody RecommendedDrink body);
    
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/recommendedDrink?mealId=1
     *
     * @param mealId
     */
    @DeleteMapping(value = "/recommendedDrink")
    void deleteRecommendedDrinks(@RequestParam(value = "mealId", required = true)  int mealId);
}