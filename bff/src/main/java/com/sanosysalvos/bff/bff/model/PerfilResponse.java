package com.sanosysalvos.bff.bff.model;


import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PerfilResponse {
    private Map<String, Object> usuario;
    private List<Map<String, Object>> mascotas;
}