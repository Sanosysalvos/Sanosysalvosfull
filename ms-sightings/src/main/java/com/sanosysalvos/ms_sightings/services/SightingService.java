package com.sanosysalvos.ms_sightings.services;

import com.sanosysalvos.ms_sightings.dtos.SightingRequestDTO;
import com.sanosysalvos.ms_sightings.dtos.SightingResponseDTO;
import com.sanosysalvos.ms_sightings.mappers.SightingMapper;
import com.sanosysalvos.ms_sightings.models.Sighting;
import com.sanosysalvos.ms_sightings.repositories.SightingRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SightingService {

    private final SightingRepository sightingRepository;
    private final SightingMapper sightingMapper;

    // GeometryFactory con SRID 4326 (WGS84 — estándar GPS)
    private static final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public SightingResponseDTO crearAvistamiento(SightingRequestDTO dto) {
        Sighting sighting = sightingMapper.toEntity(dto);

        // --- REPARACIÓN QUIRÚRGICA: Soporta petId null para flujo predictivo ---
        sighting.setPetId(dto.getPetId() != null && !dto.getPetId().isBlank() 
            ? UUID.fromString(dto.getPetId()) 
            : null);

        // Crear el punto geográfico PostGIS desde lat/lng
        // IMPORTANTE: JTS usa (longitud, latitud) — el orden es X, Y
        Point point = geometryFactory.createPoint(
            new Coordinate(dto.getLongitud(), dto.getLatitud())
        );
        sighting.setLocation(point);

        Sighting guardado = sightingRepository.save(sighting);
        return sightingMapper.toResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<SightingResponseDTO> obtenerPorMascota(UUID petId) {
        return sightingRepository
                .findByPetIdOrderByFechaAvistamientoDesc(petId)
                .stream()
                .map(sightingMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SightingResponseDTO> obtenerPorUsuario(String reporterUid) {
        return sightingRepository
                .findByReporterUidOrderByFechaAvistamientoDesc(reporterUid)
                .stream()
                .map(sightingMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SightingResponseDTO obtenerPorId(UUID id) {
        Sighting sighting = sightingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avistamiento no encontrado con ID: " + id));
        return sightingMapper.toResponseDTO(sighting);
    }

    // --- REPARACIÓN QUIRÚRGICA 2: Ahora también actualiza el petId que descubrió el motor predictivo ---
    @Transactional
    public void actualizarResultadoMatching(UUID sightingId,
                                            UUID petId, // <-- Añadido para guardar el match predictivo
                                            java.math.BigDecimal porcentaje,
                                            boolean notificacionEnviada) {
        Sighting sighting = sightingRepository.findById(sightingId)
                .orElseThrow(() -> new RuntimeException("Avistamiento no encontrado: " + sightingId));
        
        // Si el avistamiento original no tenía mascota asignada, le asociamos el ganador predictivo
        if (sighting.getPetId() == null && petId != null) {
            sighting.setPetId(petId);
        }
        
        sighting.setPorcentajeCoincidencia(porcentaje);
        sighting.setNotificacionEnviada(notificacionEnviada);
        sightingRepository.save(sighting);
    }

    @Transactional(readOnly = true)
    public List<SightingResponseDTO> obtenerRecientes(Integer horas) {
        java.time.LocalDateTime fechaCorte = java.time.LocalDateTime.now().minusHours(horas);
        System.out.println("ms-sightings: Buscando avistamientos posteriores a: " + fechaCorte);
        
        return sightingRepository
                .findByFechaAvistamientoAfterOrderByFechaAvistamientoDesc(fechaCorte)
                .stream()
                .map(sightingMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}