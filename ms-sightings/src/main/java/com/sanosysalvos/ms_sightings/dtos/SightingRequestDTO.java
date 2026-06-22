package com.sanosysalvos.ms_sightings.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SightingRequestDTO {

    // Permite que sea opcional para el flujo predictivo de ms-matching
    private String petId;

    @NotBlank(message = "El UID del reportante es obligatorio")
    private String reporterUid;

    @NotNull(message = "La latitud es obligatoria")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitud;

    private String comentario;

    // URL de Cloudinary (opcional)
    private String fotoUrl;
}