// ms-pets - dtos/PetHeatmapDTO.java
package com.sanosysalvos.ms_pets.dtos;
import com.sanosysalvos.ms_pets.models.Pet; 
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class PetHeatmapDTO {
    private UUID id;
    private String nombre;
    private String especie;
    private Double latitud;
    private Double longitud;
    private LocalDate fechaPerdida;
    private String estado;
    
    // Constructor desde Entity Pet
    public PetHeatmapDTO(Pet pet) {
        this.id = pet.getId();
        this.nombre = pet.getNombre();
        this.especie = pet.getEspecie();
        this.latitud = pet.getLatitud();
        this.longitud = pet.getLongitud();
        this.fechaPerdida = pet.getFechaPerdida();
        this.estado = pet.getEstado().name();
    }
}