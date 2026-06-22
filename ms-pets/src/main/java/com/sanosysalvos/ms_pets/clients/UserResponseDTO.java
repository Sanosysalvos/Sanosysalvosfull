package com.sanosysalvos.ms_pets.clients;

import lombok.Data;
import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID id;
    private String firebaseUid;
    private String nombre;
    private String email;
}
