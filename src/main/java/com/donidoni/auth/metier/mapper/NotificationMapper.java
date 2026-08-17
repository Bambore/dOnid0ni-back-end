package com.donidoni.auth.metier.mapper;

import com.donidoni.auth.crud.mapper.EntityMapper;
import com.donidoni.auth.metier.domain.Notification;
import com.donidoni.auth.metier.dto.NotificationCreateDto;
import com.donidoni.auth.metier.dto.NotificationResponseDto;
import com.donidoni.auth.metier.dto.NotificationUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper extends EntityMapper<Notification, NotificationCreateDto, NotificationUpdateDto, NotificationResponseDto> {

    @Override
    @Mapping(target = "utilisateur", ignore = true)
    Notification toEntity(NotificationCreateDto createDto);

    @Override
    @Mapping(target = "utilisateur", ignore = true)
    void updateEntity(NotificationUpdateDto updateDto, @MappingTarget Notification entity);

    @Override
    @Mapping(target = "utilisateurId", source = "utilisateur.id")
    NotificationResponseDto toResponse(Notification entity);
}
