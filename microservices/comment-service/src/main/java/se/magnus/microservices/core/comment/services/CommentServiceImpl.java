package se.magnus.microservices.core.comment.services;

import org.reactivestreams.Publisher;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.comment.CommentService;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.microservices.core.comment.persistence.CommentEntity;
import se.magnus.microservices.core.comment.persistence.CommentRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;
import java.util.function.Supplier;

import static java.util.logging.Level.FINE;

@RestController
public class CommentServiceImpl implements CommentService {

	private static final Logger LOG = LoggerFactory.getLogger(CommentServiceImpl.class);

	private final ServiceUtil serviceUtil;
	private final CommentMapper mapper;
	private final CommentRepository repository;

	private final Scheduler scheduler;

	@Autowired
	public CommentServiceImpl(Scheduler scheduler, ServiceUtil serviceUtil, CommentMapper mapper, CommentRepository repository) {
		this.scheduler = scheduler;
		this.serviceUtil = serviceUtil;
		this.mapper = mapper;
		this.repository = repository;
	}

	@Override
	public Flux<Comment> getComments(int mealId) {
		if (mealId < 1)
			throw new InvalidInputException("Invalid mealId: " + mealId);

		LOG.info("Will get comments for meal with id={}", mealId);

		return asyncFlux(() -> Flux.fromIterable(getByMealId(mealId))).log(null, FINE);
	}
	protected List<Comment> getByMealId(int mealId) {

		List<CommentEntity> entityList = repository.findByMealId(mealId);
		List<Comment> list = mapper.entityListToApiList(entityList);
		list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

		LOG.debug("getComments: response size: {}", list.size());

		return list;
	}

	@Override
	public Comment createComment(Comment body) {
		if (body.getMealId() < 1) throw new InvalidInputException("Invalid mealId: " + body.getMealId());

		try {
			CommentEntity entity = mapper.apiToEntity(body);
			CommentEntity newEntity = repository.save(entity);

            LOG.debug("createComment: created a comment entity: {}/{}", body.getMealId(), body.getCommentId());
            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dke) {
            throw new InvalidInputException("Duplicate key, Meal Id: " + body.getMealId() + ", Comment Id:" + body.getCommentId());
        }
	}

	@Override
	public void deleteComments(int mealId) {
		if (mealId < 1) throw new InvalidInputException("Invalid mealId: " + mealId);

		LOG.debug("deleteComments: tries to delete comment for the meal with mealId: {}", mealId);
        repository.deleteAll(repository.findByMealId(mealId));
	}

	private <T> Flux<T> asyncFlux(Supplier<Publisher<T>> publisherSupplier) {
		return Flux.defer(publisherSupplier).subscribeOn(scheduler);
	}

}
