package com.sanosysalvos.ms_sightings.mappers;

import com.sanosysalvos.ms_sightings.dtos.SightingRequestDTO;
import com.sanosysalvos.ms_sightings.dtos.SightingResponseDTO;
import com.sanosysalvos.ms_sightings.models.Sighting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SightingMapper {

    // Ignoramos location y petId porque los calculamos en el servicio
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "petId", ignore = true)
    @Mapping(target = "porcentajeCoincidencia", ignore = true)
    @Mapping(target = "notificacionEnviada", ignore = true)
    @Mapping(target = "fechaAvistamiento", ignore = true)
    Sighting toEntity(SightingRequestDTO dto);

    SightingResponseDTO toResponseDTO(Sighting sighting);
}