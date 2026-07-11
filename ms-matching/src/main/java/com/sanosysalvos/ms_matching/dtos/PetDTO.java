package com.sanosysalvos.ms_matching.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PetDTO {

    private String id;
    private String userUid;
    private String nombre;
    private String especie;
    private String color;
    private String edad;
    private String tamanio;
    private String descripcion;
    private LocalDate fechaPerdida;
    private String estado;
    private Double latitud;
    private Double longitud;
    private String foto;
    private String direccionFormateada;
}