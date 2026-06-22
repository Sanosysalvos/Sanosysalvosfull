package com.sanosysalvos.ms_sightings.controllers;

import com.sanosysalvos.ms_sightings.dtos.SightingRequestDTO;
import com.sanosysalvos.ms_sightings.dtos.SightingResponseDTO;
import com.sanosysalvos.ms_sightings.services.SightingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sightings")
@RequiredArgsConstructor
public class SightingController {

    private final SightingService sightingService;

    // POST /api/sightings — crear nuevo avistamiento
    @PostMapping
    public ResponseEntity<SightingResponseDTO> crearAvistamiento(
            @Valid @RequestBody SightingRequestDTO dto) {
        SightingResponseDTO response = sightingService.crearAvistamiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/sightings/pet/{petId} — avistamientos de una mascota
    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<SightingResponseDTO>> obtenerPorMascota(
            @PathVariable UUID petId) {
        return ResponseEntity.ok(sightingService.obtenerPorMascota(petId));
    }

    // GET /api/sightings/user/{uid} — avistamientos reportados por un usuario
    @GetMapping("/user/{uid}")
    public ResponseEntity<List<SightingResponseDTO>> obtenerPorUsuario(
            @PathVariable String uid) {
        return ResponseEntity.ok(sightingService.obtenerPorUsuario(uid));
    }

    // GET /api/sightings/{id} — detalle de un avistamiento
    @GetMapping("/{id}")
    public ResponseEntity<SightingResponseDTO> obtenerPorId(
            @PathVariable UUID id) {
        return ResponseEntity.ok(sightingService.obtenerPorId(id));
    }

    // PUT /api/sightings/{id}/matching — ms-matching actualiza el resultado (REPARADO)
    @PutMapping("/{id}/matching")
    public ResponseEntity<Void> actualizarMatching(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> payload) {
        
        BigDecimal porcentaje = new BigDecimal(payload.get("porcentaje").toString());
        boolean notificado = Boolean.parseBoolean(payload.get("notificacionEnviada").toString());
        
        // REPARACIÓN QUIRÚRGICA: Extraer el petId que descubrió el algoritmo predictivo
        UUID petId = null;
        if (payload.get("petId") != null && !payload.get("petId").toString().isBlank()) {
            petId = UUID.fromString(payload.get("petId").toString());
        }
        
        // Ahora le pasamos el combo completo al servicio (id, petId, porcentaje, notificado)
        sightingService.actualizarResultadoMatching(id, petId, porcentaje, notificado);
        
        return ResponseEntity.ok().build();
    }
}