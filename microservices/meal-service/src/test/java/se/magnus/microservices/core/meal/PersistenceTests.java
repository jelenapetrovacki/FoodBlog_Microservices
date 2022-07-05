package se.magnus.microservices.core.meal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.TestPropertySource;

import reactor.test.StepVerifier;
import se.magnus.microservices.core.meal.persistence.MealEntity;
import se.magnus.microservices.core.meal.persistence.MealRepository;

@DataMongoTest(properties = {"spring.cloud.config.enabled=false"})
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
public class PersistenceTests {

	@Autowired
	private MealRepository repository;

	private MealEntity savedEntity;

	@BeforeEach
	public void setupDb() {
		StepVerifier.create(repository.deleteAll()).verifyComplete();

		MealEntity entity = new MealEntity(1, "name", "cat", "desc", 100, "", 1);
		StepVerifier.create(repository.save(entity))
				.expectNextMatches(createdEntity -> {
					savedEntity = createdEntity;
					return areMealEqual(entity, savedEntity);
				}). verifyComplete();

	}

	@Test
	public void create() {

		MealEntity newEntity = new MealEntity(2, "name", "cat", "desc", 100, "", 1);
		StepVerifier.create(repository.save(newEntity))
				.expectNextMatches(createdEntity ->
						newEntity.getMealId() == createdEntity.getMealId())
				.verifyComplete();

		StepVerifier.create(repository.findById(newEntity.getId()))
				.expectNextMatches(foundEntity -> areMealEqual(newEntity, foundEntity))
				.verifyComplete();

		StepVerifier.create(repository.count()).expectNext(2l).verifyComplete();
	}

	@Test
	public void update() {
		savedEntity.setMealName("nameUpdated");
		StepVerifier.create(repository.save(savedEntity))
				.expectNextMatches(updatedEntity -> updatedEntity.getMealName().equals("nameUpdated"))
				.verifyComplete();

		StepVerifier.create(repository.findById(savedEntity.getId()))
				.expectNextMatches(foundEntity ->
						foundEntity.getVersion() == 1 &&
								foundEntity.getMealName().equals("nameUpdated"))
				.verifyComplete();
	}

	@Test
	public void delete() {
		StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
		StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
	}

	@Test
	public void getByMealId() {
		StepVerifier.create(repository.findByMealId(savedEntity.getMealId()))
				.expectNextMatches(foundEntity -> areMealEqual(savedEntity, foundEntity))
				.verifyComplete();
	}

	@Test
	public void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		MealEntity entity1 = repository.findById(savedEntity.getId()).block();
		MealEntity entity2 = repository.findById(savedEntity.getId()).block();

		// Update the entity using the first entity object
		entity1.setMealName("n1");
		repository.save(entity1).block();

		// Update the entity using the second entity object.
		// This should fail since the second entity now holds a old version number, i.e.
		// a Optimistic Lock Error
		StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

		// Get the updated entity from the database and verify its new sate
		StepVerifier.create(repository.findById(savedEntity.getId()))
				.expectNextMatches(foundEntity ->
						foundEntity.getVersion() == 1 &&
								foundEntity.getMealName().equals("n1"))
				.verifyComplete();
	}

	@Test
	public void duplicateError() {
		MealEntity entity = new MealEntity(savedEntity.getMealId(), "name", "cat", "desc", 100, "", 1);
	  	StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
	}

	private void assertEqualsMeal(MealEntity expectedEntity, MealEntity actualEntity) {
		assertEquals(expectedEntity.getId(), actualEntity.getId());
		assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
		assertEquals(expectedEntity.getMealId(), actualEntity.getMealId());
		assertEquals(expectedEntity.getMealName(), actualEntity.getMealName());
		assertEquals(expectedEntity.getCategory(), actualEntity.getCategory());
		assertEquals(expectedEntity.getReciepeDescription(), actualEntity.getReciepeDescription());
		assertEquals(expectedEntity.getCalories(), actualEntity.getCalories());
		assertEquals(expectedEntity.getPrepartionTime(), actualEntity.getPrepartionTime());
		assertEquals(expectedEntity.getServes(), actualEntity.getServes());
	}

	private boolean areMealEqual(MealEntity expectedEntity, MealEntity actualEntity) {
		return
				(expectedEntity.getId().equals(actualEntity.getId())) &&
						(expectedEntity.getVersion() == actualEntity.getVersion()) &&
						(expectedEntity.getMealId() == actualEntity.getMealId()) &&
						(expectedEntity.getMealName().equals(actualEntity.getMealName())) &&
						(expectedEntity.getCategory().equals(actualEntity.getCategory())) &&
						(expectedEntity.getReciepeDescription().equals(actualEntity.getReciepeDescription())) &&
						(expectedEntity.getCalories() == actualEntity.getCalories()) &&
						(expectedEntity.getPrepartionTime().equals(actualEntity.getPrepartionTime())) &&
						(expectedEntity.getServes() == actualEntity.getServes());
	}

}
