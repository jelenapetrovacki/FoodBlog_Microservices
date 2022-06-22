package se.magnus.microservices.core.ingredient.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.ingredient.IngredientService;
import se.magnus.microservices.core.ingredient.persistence.IngredientEntity;
import se.magnus.microservices.core.ingredient.persistence.IngredientRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class IngredientServiceImpl implements IngredientService{

	private static final Logger LOG = LoggerFactory.getLogger(IngredientServiceImpl.class);

	private final ServiceUtil serviceUtil;
	private final IngredientMapper mapper;
	private final IngredientRepository repository;

	@Autowired
	public IngredientServiceImpl(ServiceUtil serviceUtil, IngredientMapper mapper, IngredientRepository repository) {
		this.serviceUtil = serviceUtil;
		this.mapper = mapper; 
		this.repository = repository;
	}
	
	@Override
	public Flux<Ingredient> getIngredients(int mealId) {
		
		if (mealId < 1) 
			throw new InvalidInputException("Invalid mealId: " + mealId);

		return repository.findByMealId(mealId)
				.log()
				.map(e -> mapper.entityToApi(e))
				.map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
	}

	@Override
	public Ingredient createIngredient(Ingredient body) {
		try {
			IngredientEntity entity = mapper.apiToEntity(body);
			IngredientEntity newEntity = repository.save(entity);

            LOG.debug("createIngredient: created a ingredient entity: {}/{}", body.getMealId(), body.getIngredientId());
            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Meal Id: " + body.getMealId() + ", Ingredient Id:" + body.getIngredientId());
        }
	}

	@Override
	public void deleteIngredients(int mealId) {
		LOG.debug("deleteIngredients: tries to delete ingredients for the meal with mealId: {}", mealId);
        repository.deleteAll(repository.findByMealId(mealId));
		
	}

}
