package se.magnus.microservices.core.meal.persistence;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MealRepository extends ReactiveCrudRepository<MealEntity, String> {
	Mono<MealEntity> findByMealId(int mealId);
}
