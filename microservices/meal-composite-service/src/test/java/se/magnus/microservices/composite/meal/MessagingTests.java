package se.magnus.microservices.composite.meal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.composite.meal.CommentSummary;
import se.magnus.api.composite.meal.IngredientSummary;
import se.magnus.api.composite.meal.MealAggregate;
import se.magnus.api.composite.meal.RecommendedDrinkSummary;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.api.core.meal.Meal;
import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.microservices.composite.meal.services.MealCompositeIntegration;
import java.util.concurrent.BlockingQueue;
import se.magnus.api.event.Event;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.just;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;
import static se.magnus.microservices.composite.meal.IsSameEvent.sameEventExceptCreatedAt;

@SpringBootTest(
        webEnvironment=RANDOM_PORT,
        classes = {MealCompositeServiceApplication.class, TestSecurityConfig.class },
        properties = {"spring.main.allow-bean-definition-overriding=true","eureka.client.enabled=false","spring.cloud.config.enabled=false"})
public class MessagingTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private MealCompositeIntegration.MessageSources channels;

    @Autowired
    private MessageCollector collector;

    BlockingQueue<Message<?>> queueMeals = null;
    BlockingQueue<Message<?>> queueRecommendedDrinks = null;
    BlockingQueue<Message<?>> queueIngredients = null;
    BlockingQueue<Message<?>> queueComments = null;

    @BeforeEach
    public void setUp() {
        queueMeals = getQueue(channels.outputMeals());
        queueRecommendedDrinks = getQueue(channels.outputRecommendedDrinks());
        queueIngredients = getQueue(channels.outputIngredients());
        queueComments = getQueue(channels.outputComments());
    }

    @Test
    public void createCompositeMeal1() {

        MealAggregate composite = new MealAggregate(1, "name", "category",
                "desc", 1, "1h", 1,
                null,null,null,null);
        postAndVerifyMeal(composite, OK);

        // Assert one expected new meal events queued up
        assertEquals(1, queueMeals.size());

        Event<Integer, Meal> expectedEvent = new Event(CREATE, composite.getMealId(),
                new Meal(composite.getMealId(), composite.getMealName(), composite.getCategory(), composite.getReciepeDescription(),
                        composite.getCalories(), composite.getPrepartionTime(),composite.getServes(), null));
        assertThat(queueMeals, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

        // Assert none recommendeddrinks, comments and ingredients events
        assertEquals(0, queueRecommendedDrinks.size());
        assertEquals(0, queueComments.size());
        assertEquals(0, queueIngredients.size());
    }
/*
    @Test
    public void createCompositeMeal2() {

        MealAggregate composite = new MealAggregate(1, "name", "category",
                "desc", 1, "1h", 1,
                singletonList(new IngredientSummary(1,"name1",1,"kg" )),
                singletonList(new CommentSummary(1,"author1","subj1")),
                singletonList(new RecommendedDrinkSummary(1,"name1", true)),
                null);


        postAndVerifyMeal(composite, OK);

        // Assert one create meal event queued up
        assertEquals(1, queueMeals.size());

        Event<Integer, Meal> expectedMealEvent = new Event(CREATE,
                composite.getMealId(),
                new Meal(composite.getMealId(), composite.getMealName(), composite.getCategory(), composite.getReciepeDescription(),
                        composite.getCalories(), composite.getPrepartionTime(),composite.getServes(), null));

        assertThat(queueMeals, receivesPayloadThat(sameEventExceptCreatedAt(expectedMealEvent)));

        // Assert one create recommendedDrink event queued up
        assertEquals(1, queueRecommendedDrinks.size());

        RecommendedDrinkSummary rec = composite.getRecommendedDrinks().get(0);
        Event<Integer, Meal> expectedRecommendedDrinkEvent =
                new Event(CREATE,
                        composite.getMealId(),
                        new RecommendedDrink(composite.getMealId(), rec.getRecommendedDrinkId(),
                                rec.getDrinkName(), null, rec.isNonalcoholic(),
                                null, null,null)
                        );
        assertThat(queueRecommendedDrinks, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendedDrinkEvent)));

        // Assert one create ingredient event queued up
        assertEquals(1, queueIngredients.size());

        IngredientSummary ing = composite.getIngredients().get(0);
        Event<Integer, Ingredient> expectedIngredientEvent = new Event(CREATE,
                composite.getMealId(),
                new Ingredient(composite.getMealId(), ing.getIngredientId(),
                        ing.getName(), (int) ing.getAmount(), ing.getUnitOfMeasure() , null));

        assertThat(queueIngredients, receivesPayloadThat(sameEventExceptCreatedAt(expectedIngredientEvent)));

        // Assert one create comment event queued up
        assertEquals(1, queueComments.size());

        CommentSummary com = composite.getComments().get(0);
        Event<Integer, Comment> expectedCommentEvent = new Event(CREATE,
                composite.getMealId(),
                new Comment(composite.getMealId(), com.getCommentId(),
                        com.getAuthor(), com.getSubject(), null , null, null));

        assertThat(queueComments, receivesPayloadThat(sameEventExceptCreatedAt(expectedCommentEvent)));
    }
*/
    @Test
    public void deleteCompositeMeal() {

        deleteAndVerifyMeal(1, OK);

        // Assert one delete meals event queued up
        assertEquals(1, queueMeals.size());

        Event<Integer, Meal> expectedEvent = new Event(DELETE, 1, null);
        assertThat(queueMeals, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

        // Assert one delete recommendedDrnik event queued up
        assertEquals(1, queueRecommendedDrinks.size());

        Event<Integer, Meal> expectedRecommendedDrinkEvent = new Event(DELETE, 1, null);
        assertThat(queueRecommendedDrinks, receivesPayloadThat(sameEventExceptCreatedAt(expectedRecommendedDrinkEvent)));

        // Assert one delete ingredient event queued up
        assertEquals(1, queueIngredients.size());

        Event<Integer, Meal> expectedIngredientEvent = new Event(DELETE, 1, null);
        assertThat(queueIngredients, receivesPayloadThat(sameEventExceptCreatedAt(expectedIngredientEvent)));

        // Assert one delete comment event queued up
        assertEquals(1, queueComments.size());

        Event<Integer, Meal> expectedCommentEvent = new Event(DELETE, 1, null);
        assertThat(queueComments, receivesPayloadThat(sameEventExceptCreatedAt(expectedCommentEvent)));
    }

    private BlockingQueue<Message<?>> getQueue(MessageChannel messageChannel) {
        return collector.forChannel(messageChannel);
    }

    private void postAndVerifyMeal(MealAggregate compositeMeal, HttpStatus expectedStatus) {
        client.post()
                .uri("/meal-composite")
                .body(just(compositeMeal), MealAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyMeal(int mealId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/meal-composite/" + mealId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

}
