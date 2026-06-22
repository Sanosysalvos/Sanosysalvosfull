package com.sanosysalvos.ms_matching.dtos;

import lombok.Data;

@Data
public class UserDTO {

    private String firebaseUid;
    private String nombre;
    private String email;
    private String celular;
    private String direccionResidencia;
    private Boolean isAdmin;
}