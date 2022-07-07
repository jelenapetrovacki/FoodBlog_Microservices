package se.magnus.microservices.core.meal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DuplicateKeyException;

import reactor.core.publisher.Mono;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.meal.MealService;
import se.magnus.microservices.core.meal.persistence.MealEntity;
import se.magnus.microservices.core.meal.persistence.MealRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.Random;

import static reactor.core.publisher.Mono.error;

@RestController
public class MealServiceImpl implements MealService {

	private static final Logger LOG = LoggerFactory.getLogger(MealServiceImpl.class);
	private final ServiceUtil serviceUtil;
	private final MealRepository repository;
	private final MealMapper mapper;

	@Autowired
	public MealServiceImpl(MealRepository repository, MealMapper mapper, ServiceUtil serviceUtil) {
		this.repository = repository;
		this.mapper = mapper;
		this.serviceUtil = serviceUtil;
	}

	@Override
	public Mono<Meal> getMeal(int mealId, int delay, int faultPercent) {

		if (mealId < 1)
			throw new InvalidInputException("Invalid mealId: " + mealId);

		if (delay > 0) simulateDelay(delay);

		if (faultPercent > 0) throwErrorIfBadLuck(faultPercent);

		return repository.findByMealId(mealId)
				.switchIfEmpty(error(new NotFoundException("No meal found for mealId: " + mealId)))
				.log()
				.map(e -> mapper.entityToApi(e))
				.map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
	}

	@Override
	public Meal createMeal(Meal body) {
		if (body.getMealId() < 1)
			throw new InvalidInputException("Invalid mealId: " + body.getMealId());

		MealEntity entity = mapper.apiToEntity(body);
		Mono<Meal> newEntity = repository.save(entity)
				.log()
				.onErrorMap( DuplicateKeyException.class,
						ex -> new InvalidInputException("Duplicate key, Meal Id: " + body.getMealId()))
				.map(e -> mapper.entityToApi(e));
		return newEntity.block();
	}

	@Override
	public void deleteMeal(int mealId) {
		if (mealId < 1)
			throw new InvalidInputException("Invalid mealId: " + mealId);
		LOG.debug("deleteMeal: tries to delete an entity with mealId: {}", mealId);
	        repository.findByMealId(mealId).log().map(e -> repository.delete(e)).flatMap(e -> e).block();
	}

	private void simulateDelay(int delay) {
		LOG.debug("Sleeping for {} seconds...", delay);
		try {Thread.sleep(delay * 1000);} catch (InterruptedException e) {}
		LOG.debug("Moving on...");
	}

	private void throwErrorIfBadLuck(int faultPercent) {
		int randomThreshold = getRandomNumber(1, 100);
		if (faultPercent < randomThreshold) {
			LOG.debug("We got lucky, no error occurred, {} < {}", faultPercent, randomThreshold);
		} else {
			LOG.debug("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold);
			throw new RuntimeException("Something went wrong...");
		}
	}

	private final Random randomNumberGenerator = new Random();
	private int getRandomNumber(int min, int max) {

		if (max < min) {
			throw new RuntimeException("Max must be greater than min");
		}

		return randomNumberGenerator.nextInt((max - min) + 1) + min;
	}

}
