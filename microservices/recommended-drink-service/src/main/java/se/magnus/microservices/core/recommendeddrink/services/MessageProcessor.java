package se.magnus.microservices.core.recommendeddrink.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.api.core.recommendeddrink.RecommendedDrinkService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {


    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final RecommendedDrinkService recommendedDrinkService;

    @Autowired
    public MessageProcessor(RecommendedDrinkService recommendedDrinkService) {
        this.recommendedDrinkService = recommendedDrinkService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, RecommendedDrink> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                RecommendedDrink recommendedDrink = event.getData();
                LOG.info("Create recommendedDrink with ID: {}/{}", recommendedDrink.getMealId(), recommendedDrink.getRecommendedDrinkId());
                recommendedDrinkService.createRecommendedDrink(recommendedDrink);
                break;

            case DELETE:
                int mealId = event.getKey();
                LOG.info("Delete recommendedDrink with MealID: {}", mealId);
                recommendedDrinkService.deleteRecommendedDrinks(mealId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
