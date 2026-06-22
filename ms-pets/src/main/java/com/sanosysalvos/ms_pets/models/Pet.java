package com.sanosysalvos.ms_pets.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.time.LocalDate;

@Entity
@Table(name = "pets")
@Data
public class Pet {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userUid;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String especie;

    private String color;

    private Integer edad;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "fecha_perdida", nullable = false)
    private LocalDate fechaPerdida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetStatus estado;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(name = "direccion_texto", columnDefinition = "TEXT")
    private String direccionTexto;

    @Column(columnDefinition = "TEXT")
    private String foto;
}