package se.magnus.microservices.core.recommendeddrink.services;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import se.magnus.api.core.recommendeddrink.RecommendedDrink;
import se.magnus.microservices.core.recommendeddrink.persistence.RecommendedDrinkEntity;

@Mapper(componentModel = "spring")
public interface RecommendedDrinkMapper {
	 @Mappings({
	        @Mapping(target = "serviceAddress", ignore = true)
	    })
	    RecommendedDrink entityToApi(RecommendedDrinkEntity entity);

	    @Mappings({
	        @Mapping(target = "id", ignore = true),
	        @Mapping(target = "version", ignore = true)
	    })
	    RecommendedDrinkEntity apiToEntity(RecommendedDrink api);

	    List<RecommendedDrink> entityListToApiList(List<RecommendedDrinkEntity> entity);
	    List<RecommendedDrinkEntity> apiListToEntityList(List<RecommendedDrink> api);
}

