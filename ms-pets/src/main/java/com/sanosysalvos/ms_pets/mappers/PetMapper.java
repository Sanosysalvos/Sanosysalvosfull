package com.sanosysalvos.ms_pets.mappers;
import com.sanosysalvos.ms_pets.dtos.PetRequestDTO;
import com.sanosysalvos.ms_pets.dtos.PetResponseDTO;
import com.sanosysalvos.ms_pets.models.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import java.util.UUID;
@Mapper(componentModel = "spring")
public interface PetMapper {

    @Named("stringToUuid")
    default UUID stringToUuid(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Named("uuidToString")
    default String uuidToString(UUID value) {
        return value != null ? value.toString() : null;
    }

    @Mapping(target = "userUid", source = "userUid", qualifiedByName = "uuidToString")
    PetResponseDTO toResponseDTO(Pet pet);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userUid", source = "userUid", qualifiedByName = "stringToUuid")
    @Mapping(target = "direccionTexto", source = "direccionFormateada")
    Pet toEntity(PetRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userUid", ignore = true)
    @Mapping(target = "direccionTexto", source = "direccionFormateada")
    void updateEntityFromDto(PetRequestDTO dto, @MappingTarget Pet pet);
}