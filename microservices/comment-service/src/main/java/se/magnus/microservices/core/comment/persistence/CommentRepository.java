package se.magnus.microservices.core.comment.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends CrudRepository<CommentEntity, Integer> {
	
	@Transactional(readOnly = true)
    List<CommentEntity> findByMealId(int mealId);
}
