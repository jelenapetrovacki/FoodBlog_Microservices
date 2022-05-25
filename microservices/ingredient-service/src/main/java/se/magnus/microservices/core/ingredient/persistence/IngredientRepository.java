package se.magnus.microservices.core.ingredient.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface IngredientRepository extends CrudRepository<IngredientEntity, String> {
	List<IngredientEntity> findByMealId(int mealId);
}
