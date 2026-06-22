package com.sanosysalvos.ms_matching.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultDTO {

    private String sightingId;
    private String petId;

    // Porcentaje final de coincidencia (0-100)
    private double porcentajeTotal;

    // Desglose de cada componente para trazabilidad
    private double scoreDistancia;
    private double scoreDescripcion;
    private double scoreEspecie;

    // Si se superó el umbral y se envió notificación
    private boolean umbralSuperado;
    private boolean notificacionEnviada;

    // Mensaje descriptivo del resultado
    private String mensaje;
}