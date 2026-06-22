package com.sanosysalvos.ms_users.dtos;

import lombok.Data;

@Data
public class UserResponseDTO {
    private java.util.UUID id;  // ← agregar esto
    private String firebaseUid;
    private String nombre;
    private String rut;
    private String email;
    private String celular;
    private String direccionResidencia;
    private Boolean isAdmin;
}