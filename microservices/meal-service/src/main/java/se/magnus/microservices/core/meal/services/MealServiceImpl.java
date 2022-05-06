package se.magnus.microservices.core.meal.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.meal.MealService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class MealServiceImpl implements MealService {

	private static final Logger LOG = LoggerFactory.getLogger(MealServiceImpl.class);
	private final ServiceUtil serviceUtil;

	@Autowired
	public MealServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}

	@Override
	public Meal getMeal(int mealId) {
        LOG.debug("/meal return the found meal for mealId={}", mealId);

        if (mealId < 1) throw new InvalidInputException("Invalid mealId: " + mealId);

        if (mealId == 13) throw new NotFoundException("No meal found for mealId: " + mealId);
        
		return new Meal(mealId, "name-" + mealId, "category", "description", 0, "0min", 0, serviceUtil.getServiceAddress());
	}

}
