package com.sanosysalvos.bff.bff.controller;

import com.sanosysalvos.bff.bff.model.PerfilResponse;
import com.sanosysalvos.bff.bff.services.BffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(
    origins = {
        "http://localhost:3000",
        "https://sanosysalvos-five.vercel.app",
        "https://*.vercel.app"
    },
    methods = {
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS
    },
    allowedHeaders = "*",
    allowCredentials = "true"
)
public class BffController {

    @Autowired
    private BffService bffService;

    // -------------------------------------------------------
    // USUARIOS
    // -------------------------------------------------------

    @GetMapping("/perfil/{firebaseUid}")
    public ResponseEntity<PerfilResponse> getPerfil(@PathVariable String firebaseUid) {
        try {
            PerfilResponse perfil = bffService.getPerfil(firebaseUid);
            return ResponseEntity.ok(perfil);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuarios/firebase/{firebaseUid}")
    public ResponseEntity<Map<String, Object>> getDatosDueno(
            @PathVariable String firebaseUid) {
        try {
            Map<String, Object> usuario = bffService.getUsuarioPorId(firebaseUid);
            if (usuario == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<Map> registrarUsuario(
            @RequestBody Map<String, Object> datosUsuario) {
        return bffService.registrarUsuario(datosUsuario);
    }

    @PostMapping("/users")
    public ResponseEntity<Map> registrar(@RequestBody Map<String, Object> datos) {
        return bffService.registrarUsuario(datos);
    }

    @PutMapping("/usuarios/{userId}")
    public ResponseEntity<Map> actualizarUsuario(
            @PathVariable String userId,
            @RequestBody Map<String, Object> datosUsuario) {
        return bffService.actualizarUsuario(userId, datosUsuario);
    }

    // -------------------------------------------------------
    // MASCOTAS
    // -------------------------------------------------------

    @GetMapping("/explorar")
    public ResponseEntity<List<Map<String, Object>>> explorar() {
        return ResponseEntity.ok(bffService.getMascotasParaExplorar());
    }

    @GetMapping("/mascota/{id}")
    public ResponseEntity<Map<String, Object>> getMascotaPorId(
            @PathVariable String id) {
        try {
            Map<String, Object> mascota = bffService.getMascotaPorId(id);
            return ResponseEntity.ok(mascota);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/reportar")
    public ResponseEntity<Map> reportarMascota(
            @RequestBody Map<String, Object> datosMascota) {
        return bffService.reportarMascota(datosMascota);
    }

    @PutMapping("/pets/{id}")
    public ResponseEntity<Map<String, Object>> actualizarMascota(
            @PathVariable String id,
            @RequestBody Map<String, Object> datosMascota) {
        try {
            Map<String, Object> actualizada = bffService.actualizarMascota(id, datosMascota);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/pets/{id}")
    public ResponseEntity<Void> eliminarMascota(@PathVariable String id) {
        bffService.eliminarMascota(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------
    // NOTIFICACIÓN (flujo anterior — contactar dueño)
    // -------------------------------------------------------

    @PostMapping("/notificar-avistamiento")
    public ResponseEntity<Map<String, String>> enviarNotificacion(
            @RequestBody Map<String, String> payload) {
        try {
            bffService.enviarNotificacionAvistamiento(payload);
            return ResponseEntity.ok(Map.of("message", "Notificación enviada con éxito"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo enviar la notificación"));
        }
    }

    // -------------------------------------------------------
    // AVISTAMIENTOS
    // -------------------------------------------------------

    @PostMapping("/avistar")
    public ResponseEntity<Map<String, Object>> crearAvistamiento(
            @RequestBody Map<String, Object> datosAvistamiento) {
        try {
            Map<String, Object> resultado = bffService.crearAvistamiento(datosAvistamiento);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar el avistamiento: " + e.getMessage()));
        }
    }

    @GetMapping("/avistar/mascota/{petId}")
    public ResponseEntity<List<Map<String, Object>>> getAvistamientosPorMascota(
            @PathVariable String petId) {
        return ResponseEntity.ok(bffService.getAvistamientosPorMascota(petId));
    }
    
    @GetMapping("/avistar/usuario/{uid}")
    public ResponseEntity<List<Map<String, Object>>> getAvistamientosPorUsuario(
            @PathVariable String uid) {
        return ResponseEntity.ok(bffService.getAvistamientosPorUsuario(uid));
    }

    // -------------------------------------------------------
    // MAPA DE CALOR UNIFICADO
    // -------------------------------------------------------
    @GetMapping("/heatmap")
    public ResponseEntity<Map<String, Object>> getHeatmapData(
            @RequestParam(required = false, defaultValue = "168") Integer horas,
            @RequestParam(required = false) String especie) {
        try {
            Map<String, Object> heatmapData = bffService.getHeatmapData(horas, especie);
            return ResponseEntity.ok(heatmapData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al generar mapa de calor: " + e.getMessage()));
        }
    }
}