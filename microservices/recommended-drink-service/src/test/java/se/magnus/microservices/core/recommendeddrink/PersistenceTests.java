package se.magnus.microservices.core.recommendeddrink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.TestPropertySource;

import se.magnus.microservices.core.recommendeddrink.persistence.RecommendedDrinkEntity;
import se.magnus.microservices.core.recommendeddrink.persistence.RecommendedDrinkRepository;


@DataMongoTest
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")
public class PersistenceTests {
	
	@Autowired
	private RecommendedDrinkRepository repository;

	private RecommendedDrinkEntity savedEntity;

	@BeforeEach
	public void setupDb() {
		repository.deleteAll().block();

		//int mealId, int recommendedDrinkId, String drinkName, String drinkType, boolean nonalcoholic, String glassType, String drinkBrand
		RecommendedDrinkEntity entity = new RecommendedDrinkEntity(1, 1, "name", "type", true, "glass", "brand");
		savedEntity = repository.save(entity).block();

		assertEqualsRecommendedDrnik(entity, savedEntity);
	}
	
	@Test
	public void create() {

		RecommendedDrinkEntity newEntity = new RecommendedDrinkEntity(1, 2, "name", "type", true, "glass", "brand");
		repository.save(newEntity).block();

		RecommendedDrinkEntity foundEntity = repository.findById(newEntity.getId()).block();
		assertEqualsRecommendedDrnik(newEntity, foundEntity);

		assertEquals(2, (long) repository.count().block());
	}

	@Test
	public void update() {
		savedEntity.setDrinkName("nameUpdated");
		repository.save(savedEntity).block();

		RecommendedDrinkEntity foundEntity = repository.findById(savedEntity.getId()).block();
		assertEquals(1, (long) foundEntity.getVersion());
		assertEquals("nameUpdated", foundEntity.getDrinkName());
	}

	@Test
	public void delete() {
		repository.delete(savedEntity);
		assertFalse(repository.existsById(savedEntity.getId()).block());
	}

	@Test
	public void getByMealId() {
		List<RecommendedDrinkEntity> entityList = repository.findByMealId(savedEntity.getMealId()).collectList().block();

		assertEquals(entityList.size(), 1);
        assertEqualsRecommendedDrnik(savedEntity, entityList.get(0));
	}

	@Test
	public void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		RecommendedDrinkEntity entity1 = repository.findById(savedEntity.getId()).block();
		RecommendedDrinkEntity entity2 = repository.findById(savedEntity.getId()).block();

		// Update the entity using the first entity object
		entity1.setDrinkName("n1");
		repository.save(entity1).block();

		// Update the entity using the second entity object.
		// This should fail since the second entity now holds a old version number, i.e.
		// a Optimistic Lock Error
		try {
			entity2.setDrinkName("n2");
			repository.save(entity2).block();

			fail("Expected an OptimisticLockingFailureException");
		} catch (OptimisticLockingFailureException e) {
		}

		// Get the updated entity from the database and verify its new sate
		RecommendedDrinkEntity updatedEntity = repository.findById(savedEntity.getId()).block();
		assertEquals(1, (int) updatedEntity.getVersion());
		assertEquals("n1", updatedEntity.getDrinkName());
	}
	
	private void assertEqualsRecommendedDrnik(RecommendedDrinkEntity expectedEntity, RecommendedDrinkEntity actualEntity) {
		assertEquals(expectedEntity.getId(), actualEntity.getId());
		assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
		assertEquals(expectedEntity.getMealId(), actualEntity.getMealId());
		assertEquals(expectedEntity.getRecommendedDrinkId(), actualEntity.getRecommendedDrinkId());
		assertEquals(expectedEntity.getDrinkName(), actualEntity.getDrinkName());
		assertEquals(expectedEntity.getDrinkType(), actualEntity.getDrinkType());
		assertEquals(expectedEntity.isNonalcoholic(), actualEntity.isNonalcoholic());
		assertEquals(expectedEntity.getGlassType(), actualEntity.getGlassType());
		assertEquals(expectedEntity.getDrinkBrand(), actualEntity.getDrinkBrand());
	}

}
