package se.magnus.microservices.core.ingredient.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.ingredient.IngredientService;
import se.magnus.api.event.Event;
import se.magnus.util.exceptions.EventProcessingException;

@EnableBinding(Sink.class)
public class MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    private final IngredientService ingredientService;

    @Autowired
    public MessageProcessor(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Ingredient> event) {

        LOG.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                Ingredient ingredient = event.getData();
                LOG.info("Create ingredient with ID: {}/{}", ingredient.getMealId(), ingredient.getIngredientId());
                ingredientService.createIngredient(ingredient);
                break;

            case DELETE:
                int mealId = event.getKey();
                LOG.info("Delete ingredients with MealID: {}", mealId);
                ingredientService.deleteIngredients(mealId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                LOG.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        LOG.info("Message processing done!");
    }
}
