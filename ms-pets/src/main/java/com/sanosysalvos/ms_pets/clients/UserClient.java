package com.sanosysalvos.ms_pets.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${ms-users.url}")
    private String msUsersUrl;

    /**
     * Obtiene el UUID del usuario a partir de su Firebase UID
     * @param firebaseUid Firebase UID como String
     * @return UUID del usuario en la base de datos
     */
    public UUID getUuidByFirebaseUid(String firebaseUid) {
        try {
            String url = msUsersUrl + "/api/users/firebase/" + firebaseUid;
            log.info("Llamando a ms-users en: {}", url);
            
            UserResponseDTO response = restTemplate.getForObject(url, UserResponseDTO.class);
            
            if (response != null && response.getId() != null) {
                log.info("Firebase UID {} resuelto a UUID {}", firebaseUid, response.getId());
                return response.getId();
            }
            
            log.warn("No se encontró usuario con Firebase UID: {}", firebaseUid);
            throw new RuntimeException("Usuario no encontrado con Firebase UID: " + firebaseUid);
        } catch (RestClientException e) {
            log.error("Error al llamar a ms-users para resolver Firebase UID: {}", firebaseUid, e);
            throw new RuntimeException("Error comunicándose con ms-users: " + e.getMessage(), e);
        }
    }
}
