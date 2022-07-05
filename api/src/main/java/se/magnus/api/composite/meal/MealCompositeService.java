
package se.magnus.api.composite.meal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Mono;

@Api(description = "REST API for composite meal information.")
public interface MealCompositeService {

    /**
     * Sample usage: curl $HOST:$PORT/meal-composite/1
     *
     * @param mealId
     * @return the composite meal info, if found, else null
     */
    @ApiOperation(
            value = "${api.meal-composite.get-composite-meal.description}",
            notes = "${api.meal-composite.get-composite-meal.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.")
        })
    @GetMapping(
        value    = "/meal-composite/{mealId}",
        produces = "application/json")
    Mono<MealAggregate> getCompositeMeal(@PathVariable int mealId);
    
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/meal-composite \
     *   -H "Content-Type: application/json" --data \
     *   '{"mealId":123,"mealName":"meal 11","category":"cat"}'
     *
     * @param body
     */
    @ApiOperation(
        value = "${api.meal-composite.create-composite-meal.description}",
        notes = "${api.meal-composite.create-composite-meal.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @PostMapping(
        value    = "/meal-composite",
        consumes = "application/json")
    Mono<Void> createCompositeMeal(@RequestBody MealAggregate body);
    
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/meal-composite/1
     *
     * @param mealId
     */
    @ApiOperation(
        value = "${api.meal-composite.delete-composite-meal.description}",
        notes = "${api.meal-composite.delete-composite-meal.notes}")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
        @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @DeleteMapping(value = "/meal-composite/{mealId}")
    Mono<Void> deleteCompositeMeal(@PathVariable int mealId);
}