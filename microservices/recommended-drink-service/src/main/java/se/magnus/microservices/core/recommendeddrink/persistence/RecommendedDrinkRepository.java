package se.magnus.microservices.core.recommendeddrink.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RecommendedDrinkRepository extends ReactiveCrudRepository<RecommendedDrinkEntity, String> {
	Flux<RecommendedDrinkEntity> findByMealId(int mealId);
}
