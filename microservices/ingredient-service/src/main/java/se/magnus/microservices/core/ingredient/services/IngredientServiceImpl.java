package se.magnus.microservices.core.ingredient.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.ingredient.IngredientService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class IngredientServiceImpl implements IngredientService{

	private static final Logger LOG = LoggerFactory.getLogger(IngredientServiceImpl.class);

	private final ServiceUtil serviceUtil;

	@Autowired
	public IngredientServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}
	
	@Override
	public List<Ingredient> getIngredients(int mealId) {
		if (mealId < 1) throw new InvalidInputException("Invalid mealId: " + mealId);

        if (mealId == 113) {
            LOG.debug("No ingredients found for mealId: {}", mealId);
            return  new ArrayList<>();
        }
        List<Ingredient> list = new ArrayList<>();
        list.add(new Ingredient(mealId, 1, "Ingredient 1", 0, "g", serviceUtil.getServiceAddress()));
        list.add(new Ingredient(mealId, 2, "Ingredient 2", 0, "g", serviceUtil.getServiceAddress()));
        list.add(new Ingredient(mealId, 3, "Ingredient 3", 0, "g", serviceUtil.getServiceAddress()));

        LOG.debug("/ingredient response size: {}", list.size());

        return list;
	}

}
