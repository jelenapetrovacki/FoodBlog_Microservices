package se.magnus.microservices.core.comment.services;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import se.magnus.api.core.comment.Comment;
import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.microservices.core.comment.persistence.CommentEntity;

@Mapper(componentModel = "spring")
public interface CommentMapper {
	 @Mappings({
	        @Mapping(target = "serviceAddress", ignore = true)
	    })
	    Comment entityToApi(CommentEntity entity);

	    @Mappings({
	        @Mapping(target = "id", ignore = true),
	        @Mapping(target = "version", ignore = true)
	    })
	    CommentEntity apiToEntity(Comment api);

	    List<Comment> entityListToApiList(List<CommentEntity> entity);
	    List<CommentEntity> apiListToEntityList(List<Comment> api);
}
