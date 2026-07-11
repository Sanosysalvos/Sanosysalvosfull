package com.sanosysalvos.ms_matching.services;

import com.sanosysalvos.ms_matching.dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final RestTemplate restTemplate;

    @Value("${ms.pets.url}")
    private String msPetsUrl;

    @Value("${ms.sightings.url}")
    private String msSightingsUrl;

    @Value("${ms.notification.url}")
    private String msNotificationUrl;

    @Value("${ms.users.url}")
    private String msUsersUrl;

    @Value("${matching.umbral.notificacion}")
    private double umbralNotificacion;

    @Value("${matching.radio.km}")
    private double radioKm;

    // Pesos del motor de coincidencias (deben sumar 100)
    private static final double PESO_DISTANCIA   = 30.0;
    private static final double PESO_DESCRIPCION = 20.0;
    private static final double PESO_ESPECIE     = 25.0;
    private static final double PESO_COLOR       = 15.0;
    private static final double PESO_TAMANIO     = 10.0;

    // ---------------------------------------------------------------
    // MÉTODO PRINCIPAL REPARADO (SOPORTA FLUJO PREDICTIVO GLOBAL)
    // ---------------------------------------------------------------
    public MatchResultDTO calcularCoincidencia(String sightingId) {
        log.info("Iniciando motor de coincidencias para sightingId: {}", sightingId);

        // 1. Obtener el avistamiento desde ms-sightings
        SightingDTO sighting = obtenerAvistamiento(sightingId);
        if (sighting == null) {
            log.error("No se encontró el avistamiento: {}", sightingId);
            return resultadoError(sightingId, "Avistamiento no encontrado");
        }

        // 2. Determinar la estrategia: ¿Viene con ID o buscamos de forma predictiva?
        List<PetDTO> mascotasAEvaluar = new ArrayList<>();

        if (sighting.getPetId() != null) {
            log.info("Flujo Directo detectado. Evaluando la mascota específica: {}", sighting.getPetId());
            PetDTO pet = obtenerMascota(sighting.getPetId());
            if (pet != null) {
                mascotasAEvaluar.add(pet);
            } else {
                log.error("No se encontró la mascota específica enviada: {}", sighting.getPetId());
                return resultadoError(sightingId, "Mascota directa no encontrada");
            }
        } else {
            log.info("Flujo Predictivo detectado (petId es null). Escaneando todas las mascotas perdidas del sistema...");
            mascotasAEvaluar = obtenerTodasLasMascotasPerdidas();
        }

        if (mascotasAEvaluar.isEmpty()) {
            log.warn("No hay mascotas perdidas disponibles en el sistema para calcular coincidencias.");
            return resultadoError(sightingId, "No hay mascotas perdidas para emparejar.");
        }

        // 3. Procesar y buscar el mejor Match de la lista
        PetDTO mejorMascota = null;
        double maxPorcentajeTotal = -1.0;
        double mejorScoreDistancia = 0.0;
        double mejorScoreDescripcion = 0.0;
        double mejorScoreEspecie = 0.0;
        double mejorScoreColor = 0.0;
        double mejorScoreTamanio = 0.0;

        for (PetDTO pet : mascotasAEvaluar) {
            double scoreDistancia   = calcularScoreDistancia(sighting, pet);
            double scoreDescripcion = calcularScoreDescripcion(sighting, pet);
            double scoreEspecie     = calcularScoreEspecie(sighting, pet);
            double scoreColor       = calcularScoreColor(sighting, pet);
            double scoreTamanio     = calcularScoreTamanio(sighting, pet);

            double porcentajeTotal =
                (scoreDistancia   * PESO_DISTANCIA   / 100.0) +
                (scoreDescripcion * PESO_DESCRIPCION / 100.0) +
                (scoreEspecie     * PESO_ESPECIE     / 100.0) +
                (scoreColor       * PESO_COLOR       / 100.0) +
                (scoreTamanio     * PESO_TAMANIO     / 100.0);

            porcentajeTotal = redondear(porcentajeTotal);

            log.info("Evaluando Mascota '{}' ({}) -> Total: {}% | Distancia: {}% | Desc: {}% | Especie: {}% | Color: {}% | Tamanio: {}%",
                    pet.getNombre(), pet.getId(), porcentajeTotal,
                    redondear(scoreDistancia * PESO_DISTANCIA / 100.0),
                    redondear(scoreDescripcion * PESO_DESCRIPCION / 100.0),
                    redondear(scoreEspecie * PESO_ESPECIE / 100.0),
                    redondear(scoreColor * PESO_COLOR / 100.0),
                    redondear(scoreTamanio * PESO_TAMANIO / 100.0));

            // Nos quedamos con el match más alto que encontremos
            if (porcentajeTotal > maxPorcentajeTotal) {
                maxPorcentajeTotal = porcentajeTotal;
                mejorMascota = pet;
                mejorScoreDistancia = scoreDistancia;
                mejorScoreDescripcion = scoreDescripcion;
                mejorScoreEspecie = scoreEspecie;
                mejorScoreColor = scoreColor;
                mejorScoreTamanio = scoreTamanio;
            }
        }

        // 4. Si encontramos un match dominante, operamos con él
        boolean notificacionEnviada = false;
        boolean umbralSuperado = maxPorcentajeTotal >= umbralNotificacion;
        String finalPetId = (mejorMascota != null) ? mejorMascota.getId() : null;

        // 5. Vincular el avistamiento con la mascota ganadora (incluso si no superó el umbral, dejamos registro del mejor candidato)
        actualizarAvistamiento(sightingId, finalPetId, maxPorcentajeTotal, false);

        // 6. Si supera el umbral, se le gatilla el correo de SendGrid al dueño
        if (umbralSuperado && mejorMascota != null) {
            log.info("¡Match Exitoso! Umbral superado ({}% >= {}%) para la mascota '{}'. Notificando...",
                    maxPorcentajeTotal, umbralNotificacion, mejorMascota.getNombre());
            notificacionEnviada = enviarNotificacion(mejorMascota, sighting, maxPorcentajeTotal);

            // Actualizar indicando que el correo voló
            actualizarAvistamiento(sightingId, finalPetId, maxPorcentajeTotal, notificacionEnviada);
        }

        // 7. Construir respuesta HTTP final para el front
        String mensaje = construirMensaje(maxPorcentajeTotal, umbralSuperado, notificacionEnviada);

        return new MatchResultDTO(
            sightingId,
            finalPetId,
            maxPorcentajeTotal,
            redondear(mejorScoreDistancia   * PESO_DISTANCIA   / 100.0),
            redondear(mejorScoreDescripcion * PESO_DESCRIPCION / 100.0),
            redondear(mejorScoreEspecie     * PESO_ESPECIE     / 100.0),
            redondear(mejorScoreColor       * PESO_COLOR       / 100.0),
            redondear(mejorScoreTamanio     * PESO_TAMANIO     / 100.0),
            umbralSuperado,
            notificacionEnviada,
            mensaje
        );
    }

    // ---------------------------------------------------------------
    // COMPONENTE 1: DISTANCIA (35%)
    // ---------------------------------------------------------------
    private double calcularScoreDistancia(SightingDTO sighting, PetDTO pet) {
        if (pet.getLatitud() == null || pet.getLongitud() == null ||
            sighting.getLatitud() == null || sighting.getLongitud() == null) {
            log.warn("Coordenadas faltantes para mascota '{}' — score distancia = 0", pet.getNombre());
            return 0.0;
        }

        double distanciaKm = calcularDistanciaHaversine(
            pet.getLatitud(),    pet.getLongitud(),
            sighting.getLatitud(), sighting.getLongitud()
        );

        if (distanciaKm >= radioKm) return 0.0;

        return ((radioKm - distanciaKm) / radioKm) * 100.0;
    }

    // ---------------------------------------------------------------
    // COMPONENTE 2: DESCRIPCIÓN (35%)
    // ---------------------------------------------------------------
    private double calcularScoreDescripcion(SightingDTO sighting, PetDTO pet) {
        String descPet      = normalizar(pet.getDescripcion());
        String descSighting = normalizar(sighting.getComentario());

        if (descPet.isEmpty() || descSighting.isEmpty()) {
            return 0.0;
        }

        Set<String> palabrasPet      = new HashSet<>(Arrays.asList(descPet.split("\\s+")));
        Set<String> palabrasSighting = new HashSet<>(Arrays.asList(descSighting.split("\\s+")));

        eliminarStopwords(palabrasPet);
        eliminarStopwords(palabrasSighting);

        if (palabrasPet.isEmpty() || palabrasSighting.isEmpty()) return 0.0;

        Set<String> interseccion = new HashSet<>(palabrasPet);
        interseccion.retainAll(palabrasSighting);

        Set<String> union = new HashSet<>(palabrasPet);
        union.addAll(palabrasSighting);

        return ((double) interseccion.size() / union.size()) * 100.0;
    }

    // ---------------------------------------------------------------
    // COMPONENTE 3: ESPECIE (30%)
    // ---------------------------------------------------------------
    public double calcularScoreColor(SightingDTO sighting, PetDTO pet) {
        String colorSighting = normalizarValor(sighting.getColor());
        String colorPet = normalizarValor(pet.getColor());

        if (colorSighting.isEmpty() || colorPet.isEmpty()) {
            return 50.0;
        }

        return colorSighting.equals(colorPet) ? 100.0 : 0.0;
    }

    public double calcularScoreTamanio(SightingDTO sighting, PetDTO pet) {
        String tamanioSighting = normalizarValor(sighting.getTamanio());
        String tamanioPet = normalizarValor(pet.getTamanio());

        if (tamanioSighting.isEmpty() || tamanioPet.isEmpty()) {
            return 50.0;
        }

        if (tamanioSighting.equals(tamanioPet)) {
            return 100.0;
        }

        Map<String, List<String>> adyacentes = new HashMap<>();
        adyacentes.put("PEQUEÑO", Arrays.asList("MEDIANO"));
        adyacentes.put("MEDIANO", Arrays.asList("PEQUEÑO", "GRANDE"));
        adyacentes.put("GRANDE", Arrays.asList("MEDIANO"));

        return adyacentes.getOrDefault(tamanioSighting, Collections.emptyList()).contains(tamanioPet) ? 50.0 : 0.0;
    }

    private double calcularScoreEspecie(SightingDTO sighting, PetDTO pet) {
    String comentario = normalizar(sighting.getComentario());
    String especie    = normalizar(pet.getEspecie());

    if (especie.isEmpty()) return 0.0;

    // 1. Coincidencia directa
    if (comentario.contains(especie)) {
        return 100.0;
    }

    // 2. Coincidencia por sinónimos
    Map<String, List<String>> sinonimos = new HashMap<>();
    sinonimos.put("perro", Arrays.asList("can", "canino", "cachorro", "perrito", "mascota"));
    sinonimos.put("gato",  Arrays.asList("felino", "gatito", "michi", "minino", "mascota"));

    List<String> aliases = sinonimos.getOrDefault(especie, Collections.emptyList());
    for (String alias : aliases) {
        if (comentario.contains(alias)) {
            return 80.0;
        }
    }

    // --- REPARACIÓN INTERRUPTOR NEUTRAL ---
    // Si el comentario NO menciona la especie de esta mascota, pero TAMPOCO menciona la especie contraria
    // (ej: el comentario no dice "perro" pero tampoco dice "gato"), le damos un 50% neutral en vez de un 0%.
    String especieContraria = especie.equals("perro") ? "gato" : "perro";
    if (!comentario.contains(especieContraria)) {
        log.info("Especie no especificada en comentario. Asignando score neutral (50%) para evitar descarte.");
        return 50.0; 
    }

    return 0.0; // Si dice explícitamente la otra especie, ahí sí es un cero absoluto.
}

    // ---------------------------------------------------------------
    // FÓRMULA HAVERSINE
    // ---------------------------------------------------------------
    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ---------------------------------------------------------------
    // HELPERS & NORMALIZADORES
    // ---------------------------------------------------------------
    private String normalizar(String texto) {
        if (texto == null) return "";
        return texto.toLowerCase()
                    .replaceAll("[áàä]", "a")
                    .replaceAll("[éèë]", "e")
                    .replaceAll("[íìï]", "i")
                    .replaceAll("[óòö]", "o")
                    .replaceAll("[úùü]", "u")
                    .replaceAll("[^a-z0-9\\s]", " ")
                    .trim();
    }

    private String normalizarValor(String valor) {
        if (valor == null) return "";
        return valor.trim().toUpperCase(Locale.ROOT);
    }

    private void eliminarStopwords(Set<String> palabras) {
        Set<String> stopwords = new HashSet<>(Arrays.asList(
            "el", "la", "los", "las", "un", "una", "unos", "unas",
            "de", "del", "al", "en", "con", "por", "para", "que",
            "es", "era", "fue", "ser", "estar", "tiene", "tenia",
            "y", "o", "a", "su", "sus", "mi", "me", "lo", "se",
            "muy", "mas", "pero", "como", "cuando", "donde", "este",
            "esta", "esto", "ese", "esa", "lo", "le", "les"
        ));
        palabras.removeAll(stopwords);
        palabras.removeIf(p -> p.length() <= 2);
    }

    private double redondear(double valor) {
        return BigDecimal.valueOf(valor)
                         .setScale(2, RoundingMode.HALF_UP)
                         .doubleValue();
    }

    private String construirMensaje(double porcentaje, boolean umbral, boolean notificado) {
        if (porcentaje < 0) return "No se encontraron candidatos válidos en la zona.";
        if (!umbral) {
            return String.format("Coincidencia óptima del %.1f%% — por debajo del umbral mínimo (%.0f%%). No se envió alerta.", porcentaje, umbralNotificacion);
        }
        if (notificado) {
            return String.format("¡Coincidencia del %.1f%%! Alerta de localización enviada con éxito al dueño.", porcentaje);
        }
        return String.format("Coincidencia del %.1f%% supera el umbral, pero ocurrió un problema en el envío de la notificación.", porcentaje);
    }

    private MatchResultDTO resultadoError(String sightingId, String mensaje) {
        return new MatchResultDTO(sightingId, null, 0, 0, 0, 0, 0, 0, false, false, "ERROR: " + mensaje);
    }

    // ---------------------------------------------------------------
    // LLAMADAS REST REFACTORIZADAS Y REPARADAS
    // ---------------------------------------------------------------
    private SightingDTO obtenerAvistamiento(String sightingId) {
        try {
            String url = msSightingsUrl + "/api/sightings/" + sightingId;
            return restTemplate.getForObject(url, SightingDTO.class);
        } catch (Exception e) {
            log.error("Error al obtener avistamiento {}: {}", sightingId, e.getMessage());
            return null;
        }
    }

    private PetDTO obtenerMascota(String petId) {
        try {
            String url = msPetsUrl + "/api/pets/" + petId;
            return restTemplate.getForObject(url, PetDTO.class);
        } catch (Exception e) {
            log.error("Error al obtener mascota {}: {}", petId, e.getMessage());
            return null;
        }
    }

    private List<PetDTO> obtenerTodasLasMascotasPerdidas() {
        try {
            // Le pega al endpoint de ms-pets que saca las mascotas en estado 'PERDIDO'
            String url = msPetsUrl + "/api/pets/estado/PERDIDO";
            PetDTO[] pets = restTemplate.getForObject(url, PetDTO[].class);
            return pets != null ? Arrays.asList(pets) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error al obtener lista global de mascotas perdidas: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

  private UserDTO obtenerDueno(String userId) {
    try {
        // Quitas el "/firebase/" de la URL para usar el endpoint de ID normal
        String url = msUsersUrl + "/api/users/" + userId; 
        return restTemplate.getForObject(url, UserDTO.class);
    } catch (Exception e) {
        log.error("Error al obtener dueño {}: {}", userId, e.getMessage());
        return null;
    }
}

    private void actualizarAvistamiento(String sightingId, String petId, double porcentaje, boolean notificado) {
        try {
            String url = msSightingsUrl + "/api/sightings/" + sightingId + "/matching";
            Map<String, Object> payload = new HashMap<>();
            payload.put("petId", petId); // Ahora inyectamos qué mascota descubrió el algoritmo predictivo
            payload.put("porcentaje", porcentaje);
            payload.put("notificacionEnviada", notificado);
            restTemplate.put(url, payload);
        } catch (Exception e) {
            log.error("Error al actualizar avistamiento {}: {}", sightingId, e.getMessage());
        }
    }

   private boolean enviarNotificacion(PetDTO pet, SightingDTO sighting, double porcentaje) {
        try {
            // Usa el método reparado apuntando al endpoint de ID en Postgres
            UserDTO dueno = obtenerDueno(pet.getUserUid());
            if (dueno == null || dueno.getEmail() == null) {
                log.error("No se pudo obtener el email del dueño para mascota: {}", pet.getId());
                return false;
            }

            // Marcadores %f listos para inyectar latitud y longitud en el enlace
            String urlMapa = String.format("https://www.google.com/maps?q=%f,%f", sighting.getLatitud(), sighting.getLongitud());

            String mensaje = String.format(
         "¡Alguien avistó a tu mascota %s!<br>" +
         "<b>Coincidencia predictiva:</b> %.1f%%.<br>" +
         "<b>Coordenadas:</b> lat %.6f, lng %.6f.<br>" +
         "<a href='%s' target='_blank' style='color: #2ec4b6; font-weight: bold; text-decoration: underline;'>Haz clic aquí para ver su ubicación</a>.<br><br>" +
         "<b>Detalles:</b> %s",
          pet.getNombre(), porcentaje, sighting.getLatitud(), sighting.getLongitud(),
         urlMapa, sighting.getComentario() != null ? sighting.getComentario() : "Sin comentarios."
        );

            String url = msNotificationUrl + "/api/notifications/send-test";
            Map<String, String> payload = new HashMap<>();
            payload.put("email",   dueno.getEmail());
            payload.put("mascota", pet.getNombre());
            payload.put("mensaje", mensaje);

            restTemplate.postForEntity(url, payload, String.class);
            log.info("Notificación enviada a {} para mascota {}", dueno.getEmail(), pet.getNombre());
            return true;
        } catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            return false;
        }
    }
}