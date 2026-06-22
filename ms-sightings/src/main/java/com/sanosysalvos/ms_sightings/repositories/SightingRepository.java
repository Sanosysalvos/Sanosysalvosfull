package com.sanosysalvos.ms_sightings.repositories;

import com.sanosysalvos.ms_sightings.models.Sighting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SightingRepository extends JpaRepository<Sighting, UUID> {

    // Todos los avistamientos de una mascota específica
    List<Sighting> findByPetIdOrderByFechaAvistamientoDesc(UUID petId);

    // Todos los avistamientos reportados por un usuario
    List<Sighting> findByReporterUidOrderByFechaAvistamientoDesc(String reporterUid);

    // Avistamientos sin notificación enviada (para reintentos)
    List<Sighting> findByNotificacionEnviadaFalse();

    List<Sighting> findByFechaAvistamientoAfterOrderByFechaAvistamientoDesc(LocalDateTime fecha);
}