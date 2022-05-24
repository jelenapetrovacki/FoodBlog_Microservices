package se.magnus.microservices.core.recommendeddrink.persistence;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface RecommendedDrinkRepository extends CrudRepository<RecommendedDrinkEntity, String> {
	Optional<RecommendedDrinkEntity> findByMealId(int mealId);
}
