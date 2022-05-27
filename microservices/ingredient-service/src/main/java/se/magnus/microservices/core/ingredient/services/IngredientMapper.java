package se.magnus.microservices.core.ingredient.services;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import se.magnus.api.core.ingredient.Ingredient;
import se.magnus.microservices.core.ingredient.persistence.IngredientEntity;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
	 @Mappings({
	        @Mapping(target = "serviceAddress", ignore = true)
	    })
	    Ingredient entityToApi(IngredientEntity entity);

	    @Mappings({
	        @Mapping(target = "id", ignore = true),
	        @Mapping(target = "version", ignore = true)
	    })
	    IngredientEntity apiToEntity(Ingredient api);

	    List<Ingredient> entityListToApiList(List<IngredientEntity> entity);
	    List<IngredientEntity> apiListToEntityList(List<Ingredient> api);
}
