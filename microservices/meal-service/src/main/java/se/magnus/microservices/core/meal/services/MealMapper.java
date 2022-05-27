package se.magnus.microservices.core.meal.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import se.magnus.api.core.meal.Meal;
import se.magnus.microservices.core.meal.persistence.MealEntity;

@Mapper(componentModel = "spring")
public interface MealMapper {
	@Mappings({
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Meal entityToApi(MealEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    MealEntity apiToEntity(Meal api);
}
