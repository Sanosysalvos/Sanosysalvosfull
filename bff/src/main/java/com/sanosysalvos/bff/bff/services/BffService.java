package com.sanosysalvos.bff.bff.services;

import com.sanosysalvos.bff.bff.model.PerfilResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BffService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.users.url}")
    private String msUsersUrl;

    @Value("${ms.pets.url}")
    private String msPetsUrl;

    @Value("${ms.notification.url}")
    private String msNotificationUrl;

    @Value("${ms.sightings.url}")
    private String msSightingsUrl;

    @Value("${ms.matching.url}")
    private String msMatchingUrl;

    // -------------------------------------------------------
    // 1. PERFIL
    // -------------------------------------------------------
    public PerfilResponse getPerfil(String firebaseUid) {
        Map<String, Object> usuario = restTemplate.getForObject(
                msUsersUrl + "/api/users/firebase/" + firebaseUid, Map.class);

        PerfilResponse perfil = new PerfilResponse();
        perfil.setUsuario(usuario);

        try {
            // Usamos el endpoint por Firebase UID directamente en ms-pets
            List<Map<String, Object>> mascotasDelUsuario = restTemplate.exchange(
                    msPetsUrl + "/api/pets/user/" + firebaseUid,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
            perfil.setMascotas(mascotasDelUsuario != null ? mascotasDelUsuario : List.of());
        } catch (Exception e) {
            System.err.println("BFF: Error al traer mascotas: " + e.getMessage());
            perfil.setMascotas(List.of());
        }
        return perfil;
    }

    // -------------------------------------------------------
    // 2. EXPLORAR
    // -------------------------------------------------------
    public List<Map<String, Object>> getMascotasParaExplorar() {
        return restTemplate.exchange(
                msPetsUrl + "/api/pets",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        ).getBody();
    }

    // -------------------------------------------------------
    // 3. DETALLE MASCOTA
    // -------------------------------------------------------
    public Map<String, Object> getMascotaPorId(String id) {
        return restTemplate.getForObject(msPetsUrl + "/api/pets/" + id, Map.class);
    }

    // -------------------------------------------------------
    // 4. REPORTAR MASCOTA
    // -------------------------------------------------------
    public ResponseEntity<Map> reportarMascota(Map<String, Object> datosMascota) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosMascota, headers);
        System.out.println("BFF: Enviando a ms-pets: " + datosMascota);
        return restTemplate.postForEntity(msPetsUrl + "/api/pets", request, Map.class);
    }

    // -------------------------------------------------------
    // 5. REGISTRAR USUARIO
    // -------------------------------------------------------
public ResponseEntity<Map> registrarUsuario(Map<String, Object> datosUsuario) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);
    try {
        return restTemplate.postForEntity(msUsersUrl + "/api/users", request, Map.class);
    } catch (org.springframework.web.client.HttpClientErrorException | 
             org.springframework.web.client.HttpServerErrorException e) {
        System.err.println("BFF: Error de ms-users: " + e.getResponseBodyAsString());
        throw e;
    }
}

    // -------------------------------------------------------
    // 6. ACTUALIZAR USUARIO
public ResponseEntity<Map> actualizarUsuario(String userId, Map<String, Object> datosUsuario) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosUsuario, headers);
    
    // userId ya es el UUID interno, úsalo directo
    String url = msUsersUrl + "/api/users/" + userId;
    return restTemplate.exchange(url, HttpMethod.PUT, request, Map.class);
}

    // -------------------------------------------------------
    // 7. ELIMINAR MASCOTA
    // -------------------------------------------------------
    public void eliminarMascota(String id) {
        restTemplate.delete(msPetsUrl + "/api/pets/" + id);
    }

    // -------------------------------------------------------
    // 8. ACTUALIZAR MASCOTA
    // -------------------------------------------------------
    public Map<String, Object> actualizarMascota(String id, Map<String, Object> datosMascota) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosMascota, headers);
        return restTemplate.exchange(
                msPetsUrl + "/api/pets/" + id,
                HttpMethod.PUT,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();
    }

    // -------------------------------------------------------
    // 9. OBTENER USUARIO POR FIREBASE UID
    // -------------------------------------------------------
    public Map<String, Object> getUsuarioPorId(String uid) {
        try {
            String url = msUsersUrl + "/api/users/firebase/" + uid;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            System.err.println("BFF: Error al obtener usuario: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------
    // 10. NOTIFICACIÓN AVISTAMIENTO (flujo anterior)
    // -------------------------------------------------------
    public void enviarNotificacionAvistamiento(Map<String, String> payload) {
        try {
            String petId = payload.get("petId");
            String mensajeUsuario = payload.get("mensaje");

            Map<String, Object> mascota = getMascotaPorId(petId);
            if (mascota == null) {
                System.err.println("BFF: No se encontró la mascota ID: " + petId);
                return;
            }

            String ownerUid = (String) mascota.get("userUid");
            String nombreMascota = (String) mascota.get("nombre");

            Map<String, Object> dueno = getUsuarioPorId(ownerUid);
            if (dueno == null || dueno.get("email") == null) {
                System.err.println("BFF: Dueño no encontrado o sin email para UID: " + ownerUid);
                return;
            }

            String emailDueno = dueno.get("email").toString();

            Map<String, String> notificationRequest = new HashMap<>();
            notificationRequest.put("email", emailDueno);
            notificationRequest.put("mascota", nombreMascota);
            notificationRequest.put("mensaje", mensajeUsuario);

            String urlFinal = msNotificationUrl + "/api/notifications/send-test";
            System.out.println("BFF: Enviando notificación a " + urlFinal + " para: " + emailDueno);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    urlFinal, notificationRequest, String.class);
            System.out.println("BFF: Respuesta ms-notification: " + response.getStatusCode());

        } catch (Exception e) {
            System.err.println("BFF: Falló el flujo de notificación: " + e.getMessage());
            throw e;
        }
    }

    // -------------------------------------------------------
    // 11. CREAR AVISTAMIENTO + DISPARAR MATCHING
    // Flujo completo:
    //   1. Crea el avistamiento en ms-sightings
    //   2. Llama a ms-matching con el sightingId recibido
    // -------------------------------------------------------
    public Map<String, Object> crearAvistamiento(Map<String, Object> datosAvistamiento) {
        // Paso 1 — Crear avistamiento en ms-sightings
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(datosAvistamiento, headers);

        ResponseEntity<Map> sightingResponse = restTemplate.postForEntity(
                msSightingsUrl + "/api/sightings", request, Map.class);

        Map<String, Object> sighting = sightingResponse.getBody();
        if (sighting == null || sighting.get("id") == null) {
            throw new RuntimeException("BFF: ms-sightings no devolvió un avistamiento válido");
        }

        String sightingId = sighting.get("id").toString();
        System.out.println("BFF: Avistamiento creado con ID: " + sightingId);

        // Paso 2 — Disparar motor de matching en ms-matching
        try {
            Map<String, String> matchRequest = new HashMap<>();
            matchRequest.put("sightingId", sightingId);

            HttpEntity<Map<String, String>> matchEntity = new HttpEntity<>(matchRequest, headers);
            ResponseEntity<Map> matchResponse = restTemplate.postForEntity(
                    msMatchingUrl + "/api/matching/calcular", matchEntity, Map.class);

            Map<String, Object> matchResult = matchResponse.getBody();
            System.out.println("BFF: Resultado matching: " + matchResult);

            // Enriquecer la respuesta con el resultado del matching
            sighting.put("matching", matchResult);

        } catch (Exception e) {
            // Si falla el matching no bloqueamos — el avistamiento ya fue guardado
            System.err.println("BFF: Error en matching (no crítico): " + e.getMessage());
            sighting.put("matching", Map.of(
                "mensaje", "Avistamiento guardado. Motor de coincidencias no disponible."
            ));
        }

        return sighting;
    }

    // -------------------------------------------------------
    // 12. OBTENER AVISTAMIENTOS DE UNA MASCOTA
    // -------------------------------------------------------
    public List<Map<String, Object>> getAvistamientosPorMascota(String petId) {
        try {
            return restTemplate.exchange(
                    msSightingsUrl + "/api/sightings/pet/" + petId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
        } catch (Exception e) {
            System.err.println("BFF: Error al obtener avistamientos: " + e.getMessage());
            return List.of();
        }
    }

    // -------------------------------------------------------
    // 13. OBTENER AVISTAMIENTOS DE UN USUARIO
    // -------------------------------------------------------
    public List<Map<String, Object>> getAvistamientosPorUsuario(String uid) {
        try {
            return restTemplate.exchange(
                    msSightingsUrl + "/api/sightings/user/" + uid,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
        } catch (Exception e) {
            System.err.println("BFF: Error al obtener avistamientos del usuario: " + e.getMessage());
            return List.of();
        }
    }
    // BffService.java - Agregar este método al final de la clase

// -------------------------------------------------------
// 14. MAPA DE CALOR (HEATMAP)
// -------------------------------------------------------
public Map<String, Object> getHeatmapData(Integer horas, String especie) {
    try {
        // 1. Llamar a ms-pets para obtener mascotas perdidas
        String petsUrl = msPetsUrl + "/api/pets/lost";
        StringBuilder urlBuilder = new StringBuilder(petsUrl);
        boolean hasParam = false;
        
        if (horas != null && horas > 0) {
            urlBuilder.append("?horas=").append(horas);
            hasParam = true;
        }
        if (especie != null && !especie.isEmpty() && !"todos".equals(especie)) {
            if (hasParam) {
                urlBuilder.append("&");
            } else {
                urlBuilder.append("?");
            }
            urlBuilder.append("especie=").append(especie);
        }
        
        System.out.println("BFF: Consultando heatmap a ms-pets: " + urlBuilder.toString());
        
        List<Map<String, Object>> mascotasPerdidas;
        try {
            mascotasPerdidas = restTemplate.exchange(
                    urlBuilder.toString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
        } catch (Exception e) {
            System.err.println("BFF: ms-pets no disponible para heatmap: " + e.getMessage());
            mascotasPerdidas = List.of();
        }
        
        // 2. (Opcional) Llamar a ms-sightings para avistamientos recientes
        List<Map<String, Object>> avistamientos = List.of();
        try {
            String sightingsUrl = msSightingsUrl + "/api/sightings/recent";
            if (horas != null && horas > 0) {
                sightingsUrl += "?horas=" + horas;
            }
            
            avistamientos = restTemplate.exchange(
                    sightingsUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            ).getBody();
        } catch (Exception e) {
            System.err.println("BFF: ms-sightings no disponible (opcional): " + e.getMessage());
            // No es crítico, seguimos solo con mascotas
        }
        
        // 3. Procesar datos para el heatmap
        List<Map<String, Object>> puntos = new java.util.ArrayList<>();
        
        // Agregar mascotas perdidas (peso completo)
        if (mascotasPerdidas != null) {
            for (Map<String, Object> mascota : mascotasPerdidas) {
                if (mascota.get("latitud") != null && mascota.get("longitud") != null) {
                    Map<String, Object> punto = new HashMap<>();
                    punto.put("latitud", mascota.get("latitud"));
                    punto.put("longitud", mascota.get("longitud"));
                    punto.put("intensidad", calcularIntensidadPerdida((String) mascota.get("fecha_perdida")));
                    punto.put("tipo", "perdida");
                    punto.put("mascotaId", mascota.get("id"));
                    punto.put("nombre", mascota.get("nombre"));
                    punto.put("especie", mascota.get("especie"));
                    punto.put("fecha", mascota.get("fecha_perdida"));
                    puntos.add(punto);
                }
            }
        }
        
        // Agregar avistamientos (peso reducido - 40% de una pérdida)
        if (avistamientos != null) {
            for (Map<String, Object> avistamiento : avistamientos) {
                if (avistamiento.get("latitud") != null && avistamiento.get("longitud") != null) {
                    Map<String, Object> punto = new HashMap<>();
                    punto.put("latitud", avistamiento.get("latitud"));
                    punto.put("longitud", avistamiento.get("longitud"));
                    double intensidadBase = calcularIntensidadAvistamiento((String) avistamiento.get("fecha_avistamiento"));
                    punto.put("intensidad", intensidadBase * 0.4); // 40% del peso
                    punto.put("tipo", "avistamiento");
                    punto.put("mascotaId", avistamiento.get("pet_id"));
                    punto.put("comentario", avistamiento.get("comentario_adicional"));
                    punto.put("fecha", avistamiento.get("fecha_avistamiento"));
                    puntos.add(punto);
                }
            }
        }
        
        // 4. Preparar respuesta (mismo formato que tus otros endpoints)
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", puntos);
        response.put("total", puntos.size());
        response.put("metadata", Map.of(
            "horas", horas != null ? horas : 168,
            "especie", especie != null ? especie : "todos",
            "timestamp", System.currentTimeMillis(),
            "fuentes", Map.of(
                "mascotas_perdidas", mascotasPerdidas != null ? mascotasPerdidas.size() : 0,
                "avistamientos", avistamientos != null ? avistamientos.size() : 0
            )
        ));
        
        return response;
        
    } catch (Exception e) {
        System.err.println("BFF: Error en getHeatmapData: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Error obteniendo datos para heatmap: " + e.getMessage(), e);
    }
}

// Método auxiliar para calcular intensidad según antigüedad de pérdida
private double calcularIntensidadPerdida(String fechaPerdidaStr) {
    if (fechaPerdidaStr == null) return 0.5;
    
    try {
        // Manejar diferentes formatos de fecha
        java.time.LocalDateTime fechaPerdida;
        if (fechaPerdidaStr.contains("T")) {
            fechaPerdida = java.time.LocalDateTime.parse(fechaPerdidaStr, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
        } else {
            fechaPerdida = java.time.LocalDate.parse(fechaPerdidaStr).atStartOfDay();
        }
        
        long horas = java.time.Duration.between(fechaPerdida, java.time.LocalDateTime.now()).toHours();
        
        if (horas <= 24) return 1.0;      // Muy reciente (primer día)
        if (horas <= 72) return 0.8;      // Reciente (3 días)
        if (horas <= 168) return 0.5;     // Una semana
        if (horas <= 720) return 0.3;     // Un mes
        return 0.2;                        // Muy antiguo
    } catch (Exception e) {
        System.err.println("Error parsing fecha: " + fechaPerdidaStr);
        return 0.5;
    }
}

// Método auxiliar para calcular intensidad según antigüedad del avistamiento
private double calcularIntensidadAvistamiento(String fechaAvistamientoStr) {
    if (fechaAvistamientoStr == null) return 0.3;
    
    try {
        java.time.LocalDateTime fechaAvistamiento = java.time.LocalDateTime.parse(
            fechaAvistamientoStr, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
        
        long horas = java.time.Duration.between(fechaAvistamiento, java.time.LocalDateTime.now()).toHours();
        
        if (horas <= 12) return 1.0;       // Muy reciente (últimas 12h)
        if (horas <= 48) return 0.7;       // Reciente (2 días)
        if (horas <= 168) return 0.4;      // Semana
        return 0.2;                         // Antiguo
    } catch (Exception e) {
        return 0.3;
    }
}
}