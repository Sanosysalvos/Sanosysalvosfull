package com.sanosysalvos.ms_matching.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SightingDTO {

    private String id;
    private String petId;
    private String reporterUid;
    private Double latitud;
    private Double longitud;
    private String especie;
    private String color;
    private String tamanio;
    private String edadAprox;
    private String comentario;
    private String fotoUrl;
    private BigDecimal porcentajeCoincidencia;
    private Boolean notificacionEnviada;
    private LocalDateTime fechaAvistamiento;
}