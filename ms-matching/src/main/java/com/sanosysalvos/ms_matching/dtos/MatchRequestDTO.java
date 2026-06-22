package com.sanosysalvos.ms_matching.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MatchRequestDTO {

    // ID del avistamiento recién creado en ms-sightings
    @NotBlank(message = "El ID del avistamiento es obligatorio")
    private String sightingId;
}