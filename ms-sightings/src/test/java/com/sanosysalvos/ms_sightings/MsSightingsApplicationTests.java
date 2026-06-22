package com.sanosysalvos.ms_sightings;

import com.sanosysalvos.ms_sightings.dtos.SightingRequestDTO;
import com.sanosysalvos.ms_sightings.dtos.SightingResponseDTO;
import com.sanosysalvos.ms_sightings.mappers.SightingMapper;
import com.sanosysalvos.ms_sightings.models.Sighting;
import com.sanosysalvos.ms_sightings.repositories.SightingRepository;
import com.sanosysalvos.ms_sightings.services.SightingService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsSightingsApplicationTests {

    @Mock
    private SightingRepository sightingRepository;

    @Mock
    private SightingMapper sightingMapper;

    @InjectMocks
    private SightingService sightingService;

    @Test
    @DisplayName("Debería crear un avistamiento correctamente procesando la geometría PostGIS")
    void crearAvistamientoExitoso() {
        // 1. GIVEN - Preparar datos simulados
        UUID mockPetId = UUID.randomUUID();
        UUID mockSightingId = UUID.randomUUID();
        
        SightingRequestDTO requestDTO = new SightingRequestDTO();
        requestDTO.setPetId(mockPetId.toString());
        requestDTO.setLatitud(-33.456);
        requestDTO.setLongitud(-70.648);
        requestDTO.setComentario("Perrito visto cerca del parque");

        Sighting mockSighting = new Sighting();
        
        Sighting sightingGuardado = new Sighting();
        sightingGuardado.setId(mockSightingId);
        sightingGuardado.setPetId(mockPetId);
        sightingGuardado.setComentario("Perrito visto cerca del parque");

        SightingResponseDTO responseDTO = new SightingResponseDTO();
        responseDTO.setId(mockSightingId); // Si tu ID también es UUID, se pasa directo. Si es String, agrégale .toString()
        responseDTO.setPetId(mockPetId);   // ✅ CORREGIDO: Se pasa como UUID nativo sin .toString()

        // Configurar comportamiento de los Mocks
        when(sightingMapper.toEntity(any(SightingRequestDTO.class))).thenReturn(mockSighting);
        when(sightingRepository.save(any(Sighting.class))).thenReturn(sightingGuardado);
        when(sightingMapper.toResponseDTO(any(Sighting.class))).thenReturn(responseDTO);

        // 2. WHEN - Ejecutar el método del servicio
        SightingResponseDTO resultado = sightingService.crearAvistamiento(requestDTO);

        // 3. THEN - Validar operaciones
        assertNotNull(resultado);
        assertEquals(responseDTO.getId(), resultado.getId());
        assertEquals(responseDTO.getPetId(), resultado.getPetId());

        // Verificaciones de comportamiento en mocks
        verify(sightingMapper, times(1)).toEntity(requestDTO);
        
        verify(sightingRepository, times(1)).save(argThat(sighting -> {
            Point location = sighting.getLocation();
            assertNotNull(location);
            assertEquals(4326, location.getSRID());
            assertEquals(requestDTO.getLongitud(), location.getX());
            assertEquals(requestDTO.getLatitud(), location.getY());
            return true;
        }));
        
        verify(sightingMapper, times(1)).toResponseDTO(sightingGuardado);
    }
}