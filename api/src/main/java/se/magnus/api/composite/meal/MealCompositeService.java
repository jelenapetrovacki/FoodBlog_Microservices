
package se.magnus.api.composite.meal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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
    MealAggregate getMeal(@PathVariable int mealId);
}