package se.magnus.microservices.core.meal.persistence;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface MealRepository extends PagingAndSortingRepository<MealEntity, String> {
	Optional<MealEntity> findByMealId(int mealId);
}
