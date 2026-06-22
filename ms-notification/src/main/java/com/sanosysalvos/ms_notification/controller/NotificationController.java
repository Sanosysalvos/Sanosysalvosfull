package com.sanosysalvos.ms_notification.controller;

import com.sanosysalvos.ms_notification.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications") // Es buena práctica definir la ruta base aquí
public class NotificationController { // <-- Abre la clase

    @Autowired
    private EmailService emailService;
@PostMapping("/send-test")
public String enviarPrueba(@RequestBody Map<String, String> body) {
    // 🟢 LOG DEL CONTROLADOR (Muestra el hilo web principal)
    System.out.println("👉 [HILO CONTROLADOR] Petición del BFF recibida en: " + Thread.currentThread().getName());
    
    String email = body.get("email");
    String mascota = body.get("mascota");
    String mensajeUsuario = body.get("mensaje"); 
    String asunto = body.getOrDefault("asunto", "Alerta de avistamiento de " + mascota);
    String cuerpoFinal = mensajeUsuario;
    
    emailService.enviarCorreo(email, asunto, cuerpoFinal);
    
    return "Correo enviado a " + email;
}
}