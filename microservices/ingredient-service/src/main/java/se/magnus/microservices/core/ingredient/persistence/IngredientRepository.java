package se.magnus.microservices.core.ingredient.persistence;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface IngredientRepository extends CrudRepository<IngredientEntity, String> {
	Optional<IngredientEntity> findByMealId(int mealId);
}
