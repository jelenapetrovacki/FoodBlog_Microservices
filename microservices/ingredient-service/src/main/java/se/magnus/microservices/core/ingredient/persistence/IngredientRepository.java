package se.magnus.microservices.core.ingredient.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface IngredientRepository extends ReactiveCrudRepository<IngredientEntity, String> {
	Flux<IngredientEntity> findByMealId(int mealId);
}
