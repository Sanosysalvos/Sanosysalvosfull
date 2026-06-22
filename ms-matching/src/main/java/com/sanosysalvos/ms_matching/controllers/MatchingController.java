package com.sanosysalvos.ms_matching.controllers;

import com.sanosysalvos.ms_matching.dtos.MatchRequestDTO;
import com.sanosysalvos.ms_matching.dtos.MatchResultDTO;
import com.sanosysalvos.ms_matching.services.MatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    // POST /api/matching/calcular
    // El BFF llama a este endpoint después de crear un avistamiento
    @PostMapping("/calcular")
    public ResponseEntity<MatchResultDTO> calcular(
            @Valid @RequestBody MatchRequestDTO request) {
        log.info("Solicitud de matching recibida para sightingId: {}",
                request.getSightingId());
        MatchResultDTO result = matchingService.calcularCoincidencia(
                request.getSightingId());
        return ResponseEntity.ok(result);
    }

    // GET /api/matching/health — verificar que el servicio está vivo
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ms-matching OK");
    }
}