package se.magnus.microservices.core.recommendeddrink.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.api.core.recommendeddrink.RecommendedDrinkService;
import se.magnus.microservices.core.recommendeddrink.persistence.RecommendedDrinkEntity;
import se.magnus.microservices.core.recommendeddrink.persistence.RecommendedDrinkRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class RecommendedDrinkServiceImpl implements RecommendedDrinkService {

	private static final Logger LOG = LoggerFactory.getLogger(RecommendedDrinkServiceImpl.class);

	private final ServiceUtil serviceUtil;
	private final RecommendedDrinkMapper mapper;
	private final RecommendedDrinkRepository repository;

	@Autowired
	public RecommendedDrinkServiceImpl(ServiceUtil serviceUtil, RecommendedDrinkMapper mapper,
			RecommendedDrinkRepository repository) {
		this.serviceUtil = serviceUtil;
		this.mapper = mapper;
		this.repository = repository;
	}

	@Override
	public Flux<RecommendedDrink> getRecommendedDrinks(int mealId) {
		if (mealId < 1)
			throw new InvalidInputException("Invalid mealId: " + mealId);

		return repository.findByMealId(mealId)
				.log()
				.map(e -> mapper.entityToApi(e))
				.map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
	}

	@Override
	public RecommendedDrink createRecommendedDrink(RecommendedDrink body) {
		if (body.getMealId() < 1) throw new InvalidInputException("Invalid mealId: " + body.getMealId());

		RecommendedDrinkEntity entity = mapper.apiToEntity(body);
		Mono<RecommendedDrink> newEntity = repository.save(entity)
				.log()
				.onErrorMap(
						DuplicateKeyException.class,
						ex -> new InvalidInputException("Duplicate key, Meal Id: " + body.getMealId()
								+ ", RecommendedDrink Id:" + body.getRecommendedDrinkId()))
				.map(e -> mapper.entityToApi(e));

		return newEntity.block();

	}

	@Override
	public void deleteRecommendedDrinks(int mealId) {
		if (mealId < 1) throw new InvalidInputException("Invalid mealId: " + mealId);

		LOG.debug("deleteRecommendedDrniks: tries to delete recommended drniks for the meal with mealId: {}", mealId);
        repository.deleteAll(repository.findByMealId(mealId)).block();
	}

}
