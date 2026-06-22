package com.sanosysalvos.ms_pets;

import com.sanosysalvos.ms_pets.clients.UserClient;
import com.sanosysalvos.ms_pets.dtos.PetRequestDTO;
import com.sanosysalvos.ms_pets.dtos.PetResponseDTO;
import com.sanosysalvos.ms_pets.mappers.PetMapper;
import com.sanosysalvos.ms_pets.models.Pet;
import com.sanosysalvos.ms_pets.repositories.PetRepository;
import com.sanosysalvos.ms_pets.services.PetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsPetsApplicationTests {

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetMapper petMapper;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private PetService petService;

    @Test
    void debeCrearReporteExitosamente() {
        // ARRANGE
        String firebaseUidMock = "firebase-123";
        UUID userUuidMock = UUID.randomUUID();
        UUID petIdMock = UUID.randomUUID();

        PetRequestDTO requestDto = new PetRequestDTO();
        requestDto.setUserUid(firebaseUidMock);

        Pet petMock = new Pet();
        Pet petGuardadaMock = new Pet();
        petGuardadaMock.setId(petIdMock);

        PetResponseDTO responseDtoMock = new PetResponseDTO();

        // Comportamiento de los Mocks
        when(userClient.getUuidByFirebaseUid(firebaseUidMock)).thenReturn(userUuidMock);
        when(petMapper.toEntity(requestDto)).thenReturn(petMock);
        when(petRepository.save(petMock)).thenReturn(petGuardadaMock);
        when(petMapper.toResponseDTO(petGuardadaMock)).thenReturn(responseDtoMock);

        // ACT
        PetResponseDTO resultado = petService.crearReporte(requestDto);

        // ASSERT
        assertNotNull(resultado);
        verify(userClient, times(1)).getUuidByFirebaseUid(firebaseUidMock);
        verify(petRepository, times(1)).save(petMock);
    }

    @Test
    void debeObtenerTodasLasMascotasRecientes() {
        // ARRANGE
        Pet petMock = new Pet();
        List<Pet> listaMascotas = List.of(petMock);
        Page<Pet> pageMock = new PageImpl<>(listaMascotas);
        PetResponseDTO responseDtoMock = new PetResponseDTO();

        when(petRepository.findAll(any(Pageable.class))).thenReturn(pageMock);
        when(petMapper.toResponseDTO(petMock)).thenReturn(responseDtoMock);

        // ACT
        List<PetResponseDTO> resultado = petService.obtenerTodas(true);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(petRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void debeObtenerPorIdExitosamente() {
        // ARRANGE
        UUID petIdMock = UUID.randomUUID();
        Pet petMock = new Pet();
        PetResponseDTO responseDtoMock = new PetResponseDTO();

        when(petRepository.findById(petIdMock)).thenReturn(Optional.of(petMock));
        when(petMapper.toResponseDTO(petMock)).thenReturn(responseDtoMock);

        // ACT
        PetResponseDTO resultado = petService.obtenerPorId(petIdMock);

        // ASSERT
        assertNotNull(resultado);
        verify(petRepository, times(1)).findById(petIdMock);
    }

    @Test
    void debeLanzarExcepcionCuandoMascotaNoExisteAlObtenerPorId() {
        // ARRANGE
        UUID petIdInexistente = UUID.randomUUID();
        when(petRepository.findById(petIdInexistente)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            petService.obtenerPorId(petIdInexistente);
        });

        assertEquals("Mascota no encontrada con el ID: " + petIdInexistente, excepcion.getMessage());
    }

    @Test
    void debeObtenerPorUsuarioExitosamente() {
        // ARRANGE
        String firebaseUidMock = "user-123";
        UUID userIdMock = UUID.randomUUID();
        Pet petMock = new Pet();
        List<Pet> listaMascotas = List.of(petMock);
        PetResponseDTO responseDtoMock = new PetResponseDTO();

        when(userClient.getUuidByFirebaseUid(firebaseUidMock)).thenReturn(userIdMock);
        when(petRepository.findByUserUid(userIdMock)).thenReturn(listaMascotas);
        when(petMapper.toResponseDTO(petMock)).thenReturn(responseDtoMock);

        // ACT
        List<PetResponseDTO> resultado = petService.obtenerPorUsuario(firebaseUidMock);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void debeActualizarMascotaExitosamente() {
        // ARRANGE
        UUID petIdMock = UUID.randomUUID();
        PetRequestDTO requestDto = new PetRequestDTO();
        Pet petExistenteMock = new Pet();
        Pet petActualizadaMock = new Pet();
        PetResponseDTO responseDtoMock = new PetResponseDTO();

        when(petRepository.findById(petIdMock)).thenReturn(Optional.of(petExistenteMock));
        doNothing().when(petMapper).updateEntityFromDto(requestDto, petExistenteMock);
        when(petRepository.save(petExistenteMock)).thenReturn(petActualizadaMock);
        when(petMapper.toResponseDTO(petActualizadaMock)).thenReturn(responseDtoMock);

        // ACT
        PetResponseDTO resultado = petService.actualizarMascota(petIdMock, requestDto);

        // ASSERT
        assertNotNull(resultado);
        verify(petRepository, times(1)).save(petExistenteMock);
    }

    @Test
    void debeEliminarReporteExitosamente() {
        // ARRANGE
        UUID petIdMock = UUID.randomUUID();
        when(petRepository.existsById(petIdMock)).thenReturn(true);

        // ACT
        petService.eliminarReporte(petIdMock);

        // ASSERT
        verify(petRepository, times(1)).deleteById(petIdMock);
    }

    @Test
    void debeLanzarExcepcionAlEliminarSiNoExiste() {
        // ARRANGE
        UUID petIdInexistente = UUID.randomUUID();
        when(petRepository.existsById(petIdInexistente)).thenReturn(false);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            petService.eliminarReporte(petIdInexistente);
        });

        verify(petRepository, never()).deleteById(any(UUID.class));
    }
}