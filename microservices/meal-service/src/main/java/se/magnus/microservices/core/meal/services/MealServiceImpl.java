package se.magnus.microservices.core.meal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DuplicateKeyException;

import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.meal.MealService;
import se.magnus.microservices.core.meal.persistence.MealEntity;
import se.magnus.microservices.core.meal.persistence.MealRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

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
	public Meal getMeal(int mealId) {

		if (mealId < 1)
			throw new InvalidInputException("Invalid mealId: " + mealId);

		MealEntity entity = repository.findByMealId(mealId)
				.orElseThrow(() -> new NotFoundException("No meal found for mealId: " + mealId));

		Meal response = mapper.entityToApi(entity);
		response.setServiceAddress(serviceUtil.getServiceAddress());

		LOG.debug("getMeal: found mealId: {}", response.getMealId());

		return response;
	}

	@Override
	public Meal createMeal(Meal body) {
		try {
			MealEntity entity = mapper.apiToEntity(body);
			MealEntity newEntity = repository.save(entity);

			LOG.debug("createMeal: entity created for mealId: {}", body.getMealId());
			return mapper.entityToApi(newEntity);

		} catch (DuplicateKeyException dke) {
			throw new InvalidInputException("Duplicate key, Meal Id: " + body.getMealId());
		}
	}

	@Override
	public void deleteMeal(int mealId) {
		 LOG.debug("deleteMeal: tries to delete an entity with mealId: {}", mealId);
	        repository.findByMealId(mealId).ifPresent(e -> repository.delete(e));

	}

}
