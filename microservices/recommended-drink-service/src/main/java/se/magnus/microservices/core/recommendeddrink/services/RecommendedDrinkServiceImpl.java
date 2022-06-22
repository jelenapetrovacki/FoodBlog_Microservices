package se.magnus.microservices.core.recommendeddrink.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
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
		try {
			RecommendedDrinkEntity entity = mapper.apiToEntity(body);
			RecommendedDrinkEntity newEntity = repository.save(entity);

            LOG.debug("createRecommendedDrnik: created a recommended drink entity: {}/{}", body.getMealId(), body.getRecommendedDrinkId());
            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Meal Id: " + body.getMealId() + ", Recommended Drink Id:" + body.getRecommendedDrinkId());
        }
	}

	@Override
	public void deleteRecommendedDrinks(int mealId) {
		LOG.debug("deleteRecommendedDrniks: tries to delete recommended drniks for the meal with mealId: {}", mealId);
        repository.deleteAll(repository.findByMealId(mealId));

	}

}
