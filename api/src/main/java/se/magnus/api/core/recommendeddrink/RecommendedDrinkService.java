package se.magnus.api.core.recommendeddrink;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RecommendedDrinkService {

    /**
     * Sample usage: curl $HOST:$PORT/recommendeddrink?mealId=1
     *
     * @param mealId
     * @return
     */
    @GetMapping(
        value    = "/recommendeddrink",
        produces = "application/json")
    List<RecommendedDrink> getRecommendedDrinks(@RequestParam(value = "mealId", required = true) int mealId);
}