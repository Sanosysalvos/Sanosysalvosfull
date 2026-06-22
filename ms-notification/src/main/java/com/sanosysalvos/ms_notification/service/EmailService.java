package com.sanosysalvos.ms_notification.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;
    @Value("${sendgrid.template.id}")
    private String templateId;
    // Añadimos @Async si decidiste usarlo para que no bloquee el front
@Async
public void enviarCorreo(String destinatario, String asunto, String cuerpoMensaje) {
    System.out.println("🚀 [HILO SERVICIO] Procesando SendGrid de fondo en: " + Thread.currentThread().getName());
    System.out.println("LOG CRUCIAL -> Asunto recibido en el Servicio: [" + asunto + "]");
    
    Email from = new Email(fromEmail);
    Email to = new Email(destinatario);
    
    Mail mail = new Mail();
    mail.setFrom(from);

    // 🟢 INTERCEPTAMOS: ¿Es una alerta de mascota de Sanos y Salvos?
    if (asunto != null && (asunto.toLowerCase().contains("avistamiento") || asunto.toLowerCase().contains("noticias"))) {
        
        // 🛠️ EXTRAER EL NOMBRE DE LA MASCOTA DESDE EL ASUNTO
        // Si el asunto es "Alerta de avistamiento de Aemeath", saca lo que esté después de " de "
        String mascota = "tu mascota"; // Valor por defecto
        if (asunto.toLowerCase().contains(" de ")) {
            int index = asunto.toLowerCase().lastIndexOf(" de ");
            mascota = asunto.substring(index + 4).trim(); // Corta y limpia espacios
        }

        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        
        personalization.setSubject(asunto); 
        
        personalization.addDynamicTemplateData("subject", asunto);
        // 🟢 ¡AHORA SÍ! 'mascota' ya está resuelta como una variable local válida
        personalization.addDynamicTemplateData("nombre_mascota", mascota);
        personalization.addDynamicTemplateData("mensaje", cuerpoMensaje);

        mail.addPersonalization(personalization);
        
    } else {
        // ... (Tu bloque else queda exactamente igual)
            // 🟡 FLUJO NORMAL: Si es otro tipo de correo, sigue usando texto plano anterior
            Content content = new Content("text/plain", cuerpoMensaje);
            mail.setSubject(asunto);
            
            Personalization personalization = new Personalization();
            personalization.addTo(to);
            mail.addPersonalization(personalization);
            mail.addContent(content);
        }

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            
            // Log clave: si ves 202, todo salió bien.
            System.out.println("SendGrid Response Status: " + response.getStatusCode());
            
            if (response.getStatusCode() >= 400) {
                System.err.println("Error de SendGrid: " + response.getBody());
            }
            
        } catch (IOException ex) {
            System.err.println("Error de conexión con SendGrid: " + ex.getMessage());
        }
    }
}