package com.sanosysalvos.ms_users.models;

import jakarta.persistence.*; // Esto soluciona @Entity, @Table, @Id, @Column
import lombok.Data;            // Esto soluciona @Data
import java.util.UUID;          // Esto soluciona el tipo UUID
import java.time.LocalDateTime; // Esto soluciona LocalDateTime

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id; // Coincide con UUID PRIMARY KEY

    @Column(name = "firebase_uid", unique = true, nullable = false)
    private String firebaseUid; // Crucial para tu login

    private String nombre;

    @Column(name = "rut")
    private String rut;
    private String email;
    private String celular;

    @Column(name = "direccion_residencia")
    private String direccionResidencia;

    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}