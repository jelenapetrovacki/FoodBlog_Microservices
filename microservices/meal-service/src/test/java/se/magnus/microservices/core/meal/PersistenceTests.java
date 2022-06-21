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

import se.magnus.microservices.core.meal.persistence.MealEntity;
import se.magnus.microservices.core.meal.persistence.MealRepository;

@DataMongoTest
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
public class PersistenceTests {

	@Autowired
	private MealRepository repository;

	private MealEntity savedEntity;

	@BeforeEach
	public void setupDb() {
		repository.deleteAll();

		MealEntity entity = new MealEntity(1, "name", "cat", "desc", 100, "", 1);
		savedEntity = repository.save(entity);

		assertEqualsMeal(entity, savedEntity);
	}

	@Test
	public void create() {

		MealEntity newEntity = new MealEntity(2, "name", "cat", "desc", 100, "", 1);
		repository.save(newEntity);

		MealEntity foundEntity = repository.findById(newEntity.getId()).get();
		assertEqualsMeal(newEntity, foundEntity);

		assertEquals(2, repository.count());
	}

	@Test
	public void update() {
		savedEntity.setMealName("nameUpdated");
		repository.save(savedEntity);

		MealEntity foundEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, (long) foundEntity.getVersion());
		assertEquals("nameUpdated", foundEntity.getMealName());
	}

	@Test
	public void delete() {
		repository.delete(savedEntity);
		assertFalse(repository.existsById(savedEntity.getId()));
	}

	@Test
	public void getByMealId() {
		Optional<MealEntity> entity = repository.findByMealId(savedEntity.getMealId());

		assertTrue(entity.isPresent());
		assertEqualsMeal(savedEntity, entity.get());
	}

/*	@Test
	public void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		MealEntity entity1 = repository.findById(savedEntity.getId()).get();
		MealEntity entity2 = repository.findById(savedEntity.getId()).get();

		// Update the entity using the first entity object
		entity1.setMealName("n1");
		repository.save(entity1);

		// Update the entity using the second entity object.
		// This should fail since the second entity now holds a old version number, i.e.
		// a Optimistic Lock Error
		try {
			entity2.setMealName("n2");
			repository.save(entity2);

			fail("Expected an OptimisticLockingFailureException");
		} catch (OptimisticLockingFailureException e) {
		}

		// Get the updated entity from the database and verify its new sate
		MealEntity updatedEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, (int) updatedEntity.getVersion());
		assertEquals("n1", updatedEntity.getMealName());
	}
	*/
/*
	@Test
	public void duplicateError() {
		MealEntity entity = new MealEntity(savedEntity.getMealId(), "name", "cat", "desc", 100, "", 1);
	    assertThrows(DuplicateKeyException.class, () -> {
	    	repository.save(entity);
	    });
	}
*/
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

}
