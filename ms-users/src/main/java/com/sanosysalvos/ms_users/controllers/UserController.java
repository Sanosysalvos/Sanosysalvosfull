package com.sanosysalvos.ms_users.controllers;

import com.sanosysalvos.ms_users.dtos.UserRequestDTO;
import com.sanosysalvos.ms_users.dtos.UserResponseDTO;
import com.sanosysalvos.ms_users.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    @GetMapping("/firebase/{uid}")
    public ResponseEntity<UserResponseDTO> getUserByFirebaseUid(@PathVariable String uid) {
        // Si no existe, el Service lanza ResourceNotFoundException y el Global Handler responde 404
        return ResponseEntity.ok(userService.getUserProfile(uid));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> syncUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.syncUser(userRequestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.emailAlreadyExists(email));
    }

    @GetMapping("/check-rut")
    public ResponseEntity<Boolean> checkRut(@RequestParam String rut) {
        return ResponseEntity.ok(userService.rutAlreadyExists(rut));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        // Nota: Asegúrate de que tu userService tenga un método para buscar por ID interno
        // Por ejemplo: userService.getUserById(id) o userService.findById(id)
        return ResponseEntity.ok(userService.getUserById(id)); 
    }
}