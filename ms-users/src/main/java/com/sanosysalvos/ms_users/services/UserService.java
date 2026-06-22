package com.sanosysalvos.ms_users.services;

import com.sanosysalvos.ms_users.dtos.UserRequestDTO;
import com.sanosysalvos.ms_users.dtos.UserResponseDTO;
import com.sanosysalvos.ms_users.exceptions.ResourceNotFoundException;
import com.sanosysalvos.ms_users.mappers.UserMapper;
import com.sanosysalvos.ms_users.models.User;
import com.sanosysalvos.ms_users.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserProfile(String firebaseUid) {
        User user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con Firebase UID: " + firebaseUid));
        return userMapper.toResponseDTO(user);
    }


    public UserResponseDTO syncUser(UserRequestDTO userReq) {
        validateRequestBody(userReq);

        return userRepository.findByFirebaseUid(userReq.getFirebaseUid())
                .map(existingUser -> userMapper.toResponseDTO(existingUser))
                .orElseGet(() -> {
                    User newUser = userMapper.toEntity(userReq);
                    try {
                        User savedUser = userRepository.save(newUser);
                        return userMapper.toResponseDTO(savedUser);
                    } catch (Exception ex) {
                        log.error("Error al guardar usuario en la base de datos. Payload: {}. Error: {}", userReq, ex.getMessage(), ex);
                        throw new RuntimeException("Error al guardar usuario en la base de datos", ex);
                    }
                });
    }

    private void validateRequestBody(UserRequestDTO userReq) {
        if (userReq == null) {
            throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio");
        }
        if (isBlank(userReq.getFirebaseUid())) {
            throw new IllegalArgumentException("firebaseUid es obligatorio");
        }
        if (isBlank(userReq.getNombre())) {
            throw new IllegalArgumentException("nombre es obligatorio");
        }
        //if (isBlank(userReq.getRut())) {
            //throw new IllegalArgumentException("rut es obligatorio");
        //}
        if (isBlank(userReq.getEmail())) {
            throw new IllegalArgumentException("email es obligatorio");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }


    public UserResponseDTO updateUser(UUID id, UserRequestDTO userDetailsDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se pudo actualizar: Usuario no encontrado con ID: " + id));

        // El mapper ignora valores nulos y no modifica firebaseUid en actualizaciones
        userMapper.updateEntityFromDto(userDetailsDTO, user);

        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDTO(updatedUser);
    }


    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se pudo eliminar: Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public boolean emailAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean rutAlreadyExists(String rut) {
        return userRepository.existsByRut(rut);
    }

    public UserResponseDTO getUserById(UUID id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    return userMapper.toResponseDTO(user);
}
}