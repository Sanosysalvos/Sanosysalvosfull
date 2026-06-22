package com.sanosysalvos.ms_pets.repositories;

import com.sanosysalvos.ms_pets.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, UUID> {
    
    List<Pet> findByUserUid(UUID userUid);

    // 1. Para cuando SÍ pasas una fecha límite (evita el error de Postgres con los nulos)
    @Query("""
        SELECT p FROM Pet p 
        WHERE p.estado = com.sanosysalvos.ms_pets.models.PetStatus.PERDIDO
          AND p.latitud IS NOT NULL 
          AND p.longitud IS NOT NULL
          AND p.fechaPerdida >= :fechaLimite
          AND (:especie IS NULL OR p.especie = :especie)
        ORDER BY p.fechaPerdida DESC
        """)
    List<Pet> findLostPetsForHeatmap(
        @Param("fechaLimite") LocalDate fechaLimite, 
        @Param("especie") String especie
    );

  // 2. REPARADO CON SQL NATIVO (Cero errores de compilación en Hibernate)
    @Query(value = """
        SELECT * FROM pets 
        WHERE estado = 'PERDIDO' 
          AND latitud IS NOT NULL 
          AND longitud IS NOT NULL 
          AND (:especie IS NULL OR especie = :especie)
        ORDER BY fecha_perdida DESC
        """, nativeQuery = true)
    List<Pet> findByEstadoAndLatitudIsNotNullAndLongitudIsNotNull(
        @Param("especie") String especie
    );
}