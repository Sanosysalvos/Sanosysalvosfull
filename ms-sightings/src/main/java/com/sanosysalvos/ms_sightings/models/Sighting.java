package com.sanosysalvos.ms_sightings.models;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pet_sightings")
@Data
public class Sighting {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    // ID de la mascota perdida (opcional para flujo predictivo)
    @Column(name = "pet_id", nullable = true) // <-- Cambiado a true
    private UUID petId;

    // Firebase UID del usuario que reporta el avistamiento
    @Column(name = "reporter_uid", nullable = false)
    private String reporterUid;

    // Coordenadas donde fue visto
    @Column(nullable = false, columnDefinition = "numeric")
    private Double latitud;

    @Column(nullable = false, columnDefinition = "numeric")
    private Double longitud;

    // Columna PostGIS — se calcula automáticamente en el servicio
    @Column(columnDefinition = "geography(Point,4326)")
    private Point location;

    // Descripción del avistamiento
    @Column(columnDefinition = "TEXT")
    private String comentario;

    // Foto opcional subida a Cloudinary
    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;

    // Resultado del motor de matching (lo escribe ms-matching)
    @Column(name = "porcentaje_coincidencia", precision = 5, scale = 2)
    private BigDecimal porcentajeCoincidencia;

    // Si ya se envió notificación al dueño
    @Column(name = "notificacion_enviada")
    private Boolean notificacionEnviada = false;

    @Column(name = "fecha_avistamiento")
    private LocalDateTime fechaAvistamiento = LocalDateTime.now();
}