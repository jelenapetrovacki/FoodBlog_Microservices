package se.magnus.microservices.core.comment.services;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.comment.CommentService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class CommentServiceImpl implements CommentService {

	private static final Logger LOG = LoggerFactory.getLogger(CommentServiceImpl.class);

	private final ServiceUtil serviceUtil;

	@Autowired
	public CommentServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}

	@Override
	public List<Comment> getComments(int mealId) {
		if (mealId < 1) throw new InvalidInputException("Invalid mealId: " + mealId);

        if (mealId == 113) {
            LOG.debug("No comments found for mealId: {}", mealId);
            return  new ArrayList<>();
        }
        List<Comment> list = new ArrayList<>();
        list.add(new Comment(mealId, 1, "Author 1", "Subject 1", "Content 1", null, serviceUtil.getServiceAddress()));
        list.add(new Comment(mealId, 2, "Author 2", "Subject 2", "Content 2", null, serviceUtil.getServiceAddress()));
        list.add(new Comment(mealId, 3, "Author 3", "Subject 3", "Content 3", null, serviceUtil.getServiceAddress()));

        LOG.debug("/comment response size: {}", list.size());

        return list;
	}

}
