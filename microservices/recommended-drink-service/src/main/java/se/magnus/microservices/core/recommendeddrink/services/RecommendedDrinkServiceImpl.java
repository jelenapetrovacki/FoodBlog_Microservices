package se.magnus.microservices.core.recommendeddrink.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.api.core.recommendeddrink.RecommendedDrinkService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class RecommendedDrinkServiceImpl implements RecommendedDrinkService {

	private static final Logger LOG = LoggerFactory.getLogger(RecommendedDrinkServiceImpl.class);

	private final ServiceUtil serviceUtil;

	@Autowired
	public RecommendedDrinkServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}
	
	@Override
	public List<RecommendedDrink> getRecommendedDrinks(int mealId) {
		if (mealId < 1) throw new InvalidInputException("Invalid mealId: " + mealId);

        if (mealId == 113) {
            LOG.debug("No drinks found for mealId: {}", mealId);
            return  new ArrayList<>();
        }
        List<RecommendedDrink> list = new ArrayList<>();
        list.add(new RecommendedDrink(mealId, 1, "Name 1", "Type 1",true, "Glass 1", "Brand 1", serviceUtil.getServiceAddress()));
        list.add(new RecommendedDrink(mealId, 2, "Name 2", "Type 2",true, "Glass 2", "Brand 2", serviceUtil.getServiceAddress()));
        list.add(new RecommendedDrink(mealId, 3, "Name 3", "Type 3",true, "Glass 3", "Brand 3", serviceUtil.getServiceAddress()));

        LOG.debug("/recommendedDrink response size: {}", list.size());

        return list;
	}

}
