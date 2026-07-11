package com.sanosysalvos.ms_matching;

import com.sanosysalvos.ms_matching.dtos.MatchResultDTO;
import com.sanosysalvos.ms_matching.dtos.PetDTO;
import com.sanosysalvos.ms_matching.dtos.SightingDTO;
import com.sanosysalvos.ms_matching.dtos.UserDTO;
import com.sanosysalvos.ms_matching.services.MatchingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsMatchingApplicationTests {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        // Inyectar manualmente los @Value que Spring metería en producción
        ReflectionTestUtils.setField(matchingService, "msSightingsUrl", "http://ms-sightings");
        ReflectionTestUtils.setField(matchingService, "msPetsUrl", "http://ms-pets");
        ReflectionTestUtils.setField(matchingService, "msUsersUrl", "http://ms-users");
        ReflectionTestUtils.setField(matchingService, "msNotificationUrl", "http://ms-notification");
        ReflectionTestUtils.setField(matchingService, "umbralNotificacion", 72.0);
        ReflectionTestUtils.setField(matchingService, "radioKm", 5.0);
    }

    @Test
    @DisplayName("Debería calcular los scores de color y tamaño con reglas normalizadas")
    void calcularScoresColorYTamanio() {
        SightingDTO sighting = new SightingDTO();
        sighting.setColor("NEGRO");
        sighting.setTamanio("MEDIANO");

        PetDTO pet = new PetDTO();
        pet.setColor("NEGRO");
        pet.setTamanio("PEQUEÑO");

        assertEquals(100.0, matchingService.calcularScoreColor(sighting, pet));
        assertEquals(50.0, matchingService.calcularScoreTamanio(sighting, pet));
    }

    @Test
    @DisplayName("Debería calcular coincidencia exitosa superando el umbral y enviando notificación")
    void calcularCoincidenciaExitoSuperaUmbral() {
        // 1. GIVEN - Preparar datos de prueba (Escenario de Coincidencia Alta)
        String sightingId = "sight-100";
        String petId = "pet-500";
        String userUid = "user-abc";

        SightingDTO mockSighting = new SightingDTO();
        mockSighting.setPetId(petId);
        mockSighting.setLatitud(-33.456); // Ubicación idéntica para máxima puntuación de distancia
        mockSighting.setLongitud(-70.648);
        mockSighting.setComentario("Se busca perrito tierno");

        PetDTO mockPet = new PetDTO();
        mockPet.setId(petId);
        mockPet.setUserUid(userUid);
        mockPet.setLatitud(-33.456);
        mockPet.setLongitud(-70.648);
        mockPet.setEspecie("perro");
        mockPet.setDescripcion("Perrito tierno perdido");

        UserDTO mockDueno = new UserDTO();
        mockDueno.setEmail("dueno@correo.com");

        // Mockear las respuestas HTTP simuladas del RestTemplate
        when(restTemplate.getForObject("http://ms-sightings/api/sightings/" + sightingId, SightingDTO.class))
                .thenReturn(mockSighting);
        
        when(restTemplate.getForObject("http://ms-pets/api/pets/" + petId, PetDTO.class))
                .thenReturn(mockPet);
        
        when(restTemplate.getForObject("http://ms-users/api/users/" + userUid, UserDTO.class))
                .thenReturn(mockDueno);

        // 2. WHEN - Ejecutar el motor de matching real
        MatchResultDTO resultado = matchingService.calcularCoincidencia(sightingId);

        // 3. THEN - Verificar las aserciones de JUnit 5
        assertNotNull(resultado);
        assertEquals(sightingId, resultado.getSightingId());
        assertEquals(petId, resultado.getPetId());
        
        // Al procesar textos similares y misma ubicación, el score supera con creces el umbral de 60.0
        assertTrue(resultado.getPorcentajeTotal() >= 70.0, "El score total debería superar el umbral actualizado");
        assertTrue(resultado.isUmbralSuperado());
        assertTrue(resultado.isNotificacionEnviada());

        // Verificar el comportamiento esperado y ejecuciones en los microservicios simulados
        verify(restTemplate, times(2)).put(eq("http://ms-sightings/api/sightings/" + sightingId + "/matching"), anyMap());
        verify(restTemplate, times(1)).postForEntity(eq("http://ms-notification/api/notifications/send-test"), anyMap(), eq(String.class));
    }
}