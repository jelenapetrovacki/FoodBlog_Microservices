package se.magnus.microservices.core.recommendeddrink.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface RecommendedDrinkRepository extends CrudRepository<RecommendedDrinkEntity, String> {
	List<RecommendedDrinkEntity> findByMealId(int mealId);
}
