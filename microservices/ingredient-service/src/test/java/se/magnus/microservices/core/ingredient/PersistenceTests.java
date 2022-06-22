package se.magnus.microservices.core.ingredient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.TestPropertySource;

import se.magnus.microservices.core.ingredient.persistence.IngredientEntity;
import se.magnus.microservices.core.ingredient.persistence.IngredientRepository;


@DataMongoTest
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
public class PersistenceTests {
	
	@Autowired
	private IngredientRepository repository;

	private IngredientEntity savedEntity;

	@BeforeEach
	public void setupDb() {
		repository.deleteAll().block();

		//int mealId, int ingredientId, String name, double amount, String unitOfMeasure
		IngredientEntity entity = new IngredientEntity(1, 1, "name", 100, "g");
		savedEntity = repository.save(entity).block();

		assertEqualsIngredients(entity, savedEntity);
	}
	
	@Test
	public void create() {

		IngredientEntity newEntity = new IngredientEntity(1, 2, "name", 100, "g");
		repository.save(newEntity).block();

		IngredientEntity foundEntity = repository.findById(newEntity.getId()).block();
		assertEqualsIngredients(newEntity, foundEntity);

		assertEquals(2, (long) repository.count().block());
	}

	@Test
	public void update() {
		savedEntity.setName("nameUpdated");
		repository.save(savedEntity).block();

		IngredientEntity foundEntity = repository.findById(savedEntity.getId()).block();
		assertEquals(1, (long) foundEntity.getVersion());
		assertEquals("nameUpdated", foundEntity.getName());
	}

	@Test
	public void delete() {
		repository.delete(savedEntity).block();
		assertFalse(repository.existsById(savedEntity.getId()).block());
	}

	@Test
	public void getByMealId() {
		List<IngredientEntity> entityList = repository.findByMealId(savedEntity.getMealId()).collectList().block();

		assertEquals(entityList.size(), 1);
        assertEqualsIngredients(savedEntity, entityList.get(0));
	}

	@Test
	public void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		IngredientEntity entity1 = repository.findById(savedEntity.getId()).block();
		IngredientEntity entity2 = repository.findById(savedEntity.getId()).block();

		// Update the entity using the first entity object
		entity1.setName("n1");
		repository.save(entity1).block();

		// Update the entity using the second entity object.
		// This should fail since the second entity now holds a old version number, i.e.
		// a Optimistic Lock Error
		try {
			entity2.setName("n2");
			repository.save(entity2).block();

			fail("Expected an OptimisticLockingFailureException");
		} catch (OptimisticLockingFailureException e) {
		}

		// Get the updated entity from the database and verify its new sate
		IngredientEntity updatedEntity = repository.findById(savedEntity.getId()).block();
		assertEquals(1, (int) updatedEntity.getVersion());
		assertEquals("n1", updatedEntity.getName());
	}

	private void assertEqualsIngredients(IngredientEntity expectedEntity, IngredientEntity actualEntity) {
		assertEquals(expectedEntity.getId(), actualEntity.getId());
		assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
		assertEquals(expectedEntity.getMealId(), actualEntity.getMealId());
		assertEquals(expectedEntity.getIngredientId(), actualEntity.getIngredientId());
		assertEquals(expectedEntity.getName(), actualEntity.getName());
		assertEquals(expectedEntity.getAmount(), actualEntity.getAmount());
		assertEquals(expectedEntity.getUnitOfMeasure(), actualEntity.getUnitOfMeasure());
	}

}
