package se.magnus.microservices.core.meal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.meal.MealService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final MealService mealService;

    @Autowired
    public MessageProcessor(MealService mealService) {
        this.mealService = mealService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Meal> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                Meal meal = event.getData();
                LOG.info("Create meal with ID: {}", meal.getMealId());
                mealService.createMeal(meal);
                break;

            case DELETE:
                int mealId = event.getKey();
                LOG.info("Delete meals with MealID: {}", mealId);
                mealService.deleteMeal(mealId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
