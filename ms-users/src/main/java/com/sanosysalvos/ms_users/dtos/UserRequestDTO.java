package com.sanosysalvos.ms_users.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequestDTO {
    @JsonAlias({"firebase_uid", "uid"})
    @NotBlank(message = "firebaseUid es obligatorio")
    private String firebaseUid; // Solo necesario en la creación

    @NotBlank(message = "nombre es obligatorio")
    private String nombre;

    //@NotBlank(message = "rut es obligatorio")
    private String rut;

    @NotBlank(message = "email es obligatorio")
    private String email;

    private String celular;

    @JsonAlias({"direccion_residencia"})
    private String direccionResidencia;
}