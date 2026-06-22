package com.sanosysalvos.ms_users;

import com.sanosysalvos.ms_users.dtos.UserRequestDTO;
import com.sanosysalvos.ms_users.dtos.UserResponseDTO;
import com.sanosysalvos.ms_users.exceptions.ResourceNotFoundException;
import com.sanosysalvos.ms_users.mappers.UserMapper;
import com.sanosysalvos.ms_users.models.User;
import com.sanosysalvos.ms_users.repositories.UserRepository;
import com.sanosysalvos.ms_users.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsUsersApplicationTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void debeObtenerTodosLosUsuarios() {
        // ARRANGE
        User userMock = new User();
        UserResponseDTO responseMock = new UserResponseDTO();
        
        when(userRepository.findAll()).thenReturn(List.of(userMock));
        when(userMapper.toResponseDTO(userMock)).thenReturn(responseMock);

        // ACT
        List<UserResponseDTO> resultado = userService.getAllUsers();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void debeObtenerPerfilDeUsuarioExitosamente() {
        // ARRANGE
        String firebaseUid = "firebase-ok-123";
        User userMock = new User();
        UserResponseDTO responseMock = new UserResponseDTO();

        when(userRepository.findByFirebaseUid(firebaseUid)).thenReturn(Optional.of(userMock));
        when(userMapper.toResponseDTO(userMock)).thenReturn(responseMock);

        // ACT
        UserResponseDTO resultado = userService.getUserProfile(firebaseUid);

        // ASSERT
        assertNotNull(resultado);
        verify(userRepository, times(1)).findByFirebaseUid(firebaseUid);
    }

    @Test
    void debeLanzarExcepcionCuandoPerfilNoExiste() {
        // ARRANGE
        String firebaseUidInex = "no-existe";
        when(userRepository.findByFirebaseUid(firebaseUidInex)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserProfile(firebaseUidInex);
        });
    }

    @Test
    void debeSincronizarUsuarioExistenteSinCrearUnoNuevo() {
        // ARRANGE
        UserRequestDTO reqDto = new UserRequestDTO();
        reqDto.setFirebaseUid("existente-123");
        
        User existingUser = new User();
        UserResponseDTO responseMock = new UserResponseDTO();

        when(userRepository.findByFirebaseUid(reqDto.getFirebaseUid())).thenReturn(Optional.of(existingUser));
        when(userMapper.toResponseDTO(existingUser)).thenReturn(responseMock);

        // ACT
        UserResponseDTO resultado = userService.syncUser(reqDto);

        // ASSERT
        assertNotNull(resultado);
        verify(userRepository, never()).save(any(User.class)); // Crucial: No debe guardar nada nuevo
    }

    @Test
    void debeCrearNuevoUsuarioEnSincronizacionSiNoExiste() {
        // ARRANGE
        UserRequestDTO reqDto = new UserRequestDTO();
        reqDto.setFirebaseUid("nuevo-123");

        User newUserMock = new User();
        User savedUserMock = new User();
        UserResponseDTO responseMock = new UserResponseDTO();

        when(userRepository.findByFirebaseUid(reqDto.getFirebaseUid())).thenReturn(Optional.empty());
        when(userMapper.toEntity(reqDto)).thenReturn(newUserMock);
        when(userRepository.save(newUserMock)).thenReturn(savedUserMock);
        when(userMapper.toResponseDTO(savedUserMock)).thenReturn(responseMock);

        // ACT
        UserResponseDTO resultado = userService.syncUser(reqDto);

        // ASSERT
        assertNotNull(resultado);
        verify(userRepository, times(1)).save(newUserMock);
    }

    @Test
    void debeActualizarUsuarioExitosamente() {
        // ARRANGE
        UUID idMock = UUID.randomUUID();
        UserRequestDTO reqDto = new UserRequestDTO();
        User userMock = new User();
        User updatedUserMock = new User();
        UserResponseDTO responseMock = new UserResponseDTO();

        when(userRepository.findById(idMock)).thenReturn(Optional.of(userMock));
        doNothing().when(userMapper).updateEntityFromDto(reqDto, userMock);
        when(userRepository.save(userMock)).thenReturn(updatedUserMock);
        when(userMapper.toResponseDTO(updatedUserMock)).thenReturn(responseMock);

        // ACT
        UserResponseDTO resultado = userService.updateUser(idMock, reqDto);

        // ASSERT
        assertNotNull(resultado);
        verify(userRepository, times(1)).save(userMock);
    }

    @Test
    void debeEliminarUsuarioExitosamente() {
        // ARRANGE
        UUID idMock = UUID.randomUUID();
        when(userRepository.existsById(idMock)).thenReturn(true);

        // ACT
        assertDoesNotThrow(() -> userService.deleteUser(idMock));

        // ASSERT
        verify(userRepository, times(1)).deleteById(idMock);
    }

    @Test
    void debeLanzarExcepcionAlEliminarSiUsuarioNoExiste() {
        // ARRANGE
        UUID idInexistente = UUID.randomUUID();
        when(userRepository.existsById(idInexistente)).thenReturn(false);

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(idInexistente);
        });
        verify(userRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void debeVerificarSiEmailYaExiste() {
        // ARRANGE
        String email = "test@correo.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // ACT
        boolean existe = userService.emailAlreadyExists(email);

        // ASSERT
        assertTrue(existe);
    }

    @Test
    void debeVerificarSiRutYaExiste() {
        // ARRANGE
        String rut = "12345678-9";
        when(userRepository.existsByRut(rut)).thenReturn(false);

        // ACT
        boolean existe = userService.rutAlreadyExists(rut);

        // ASSERT
        falseVerify: assertFalse(existe);
    }
}