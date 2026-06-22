package com.sanosysalvos.ms_pets.dtos;

import com.sanosysalvos.ms_pets.models.PetStatus;
import lombok.Data;
import java.util.UUID;
import java.time.LocalDate;

@Data
public class PetResponseDTO {
    private UUID id;
    private String userUid;
    private String nombre;
    private String especie;
    private String color;
    private Integer edad;
    private String descripcion;
    private LocalDate fechaPerdida;
    private PetStatus estado;
    private Double latitud;
    private Double longitud;
    private String direccionTexto;
    private String foto;
}