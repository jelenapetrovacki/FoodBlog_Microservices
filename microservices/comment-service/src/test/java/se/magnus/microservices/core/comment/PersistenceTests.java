package se.magnus.microservices.core.comment;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import se.magnus.microservices.core.comment.persistence.CommentEntity;
import se.magnus.microservices.core.comment.persistence.CommentRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DataJpaTest(properties = {"spring.cloud.config.enabled=false"})
@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {
	
	@Autowired
	private CommentRepository repository;

	private CommentEntity savedEntity;

	@BeforeEach
	public void setupDb() {
		repository.deleteAll();

		//int mealId, int commentId, String author, String subject, String content,	Date dateTime
		CommentEntity entity = new CommentEntity(1, 1, "jelena", "naslov", "sadrzaj", new Date());
		savedEntity = repository.save(entity);

		assertEqualsComments(entity, savedEntity);
	}
	
	@Test
	public void create() {

		CommentEntity newEntity = new CommentEntity(1, 2, "jelena", "naslov", "sadrzaj", new Date());
		repository.save(newEntity);

		CommentEntity foundEntity = repository.findById(newEntity.getId()).get();
		assertEqualsComments(newEntity, foundEntity);

		assertEquals(2, repository.count());
	}

	@Test
	public void update() {
		savedEntity.setSubject("subjectUpdated");
		repository.save(savedEntity);

		CommentEntity foundEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, (long) foundEntity.getVersion());
		assertEquals("subjectUpdated", foundEntity.getSubject());
	}

	@Test
	public void delete() {
		repository.delete(savedEntity);
		assertFalse(repository.existsById(savedEntity.getId()));
	}

	@Test
	public void getByMealId() {
		List<CommentEntity> entityList = repository.findByMealId(savedEntity.getMealId());

		assertEquals(entityList.size(), 1);
        assertEqualsComments(savedEntity, entityList.get(0));
	}

	@Test
	public void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		CommentEntity entity1 = repository.findById(savedEntity.getId()).get();
		CommentEntity entity2 = repository.findById(savedEntity.getId()).get();

		// Update the entity using the first entity object
		entity1.setSubject("s1");
		repository.save(entity1);

		// Update the entity using the second entity object.
		// This should fail since the second entity now holds a old version number, i.e.
		// a Optimistic Lock Error
		try {
			entity2.setSubject("s2");
			repository.save(entity2);

			fail("Expected an OptimisticLockingFailureException");
		} catch (OptimisticLockingFailureException e) {
		}

		// Get the updated entity from the database and verify its new sate
		CommentEntity updatedEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, (int) updatedEntity.getVersion());
		assertEquals("s1", updatedEntity.getSubject());
	}
	
	private void assertEqualsComments(CommentEntity expectedEntity, CommentEntity actualEntity) {
		assertEquals(expectedEntity.getId(), actualEntity.getId());
		assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
		assertEquals(expectedEntity.getMealId(), actualEntity.getMealId());
		assertEquals(expectedEntity.getCommentId(), actualEntity.getCommentId());
		assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
		assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
		assertEquals(expectedEntity.getContent(), actualEntity.getContent());
		assertEquals(expectedEntity.getDateTime(), actualEntity.getDateTime());
	}

}
