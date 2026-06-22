package com.sanosysalvos.ms_notification;

import com.sanosysalvos.ms_notification.service.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MsNotificationApplicationTests {

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Inyectamos los valores simulados de las propiedades @Value
        ReflectionTestUtils.setField(emailService, "apiKey", "SG.fake_api_key_valid_format_123456789");
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@sanosysalvos.com");
        ReflectionTestUtils.setField(emailService, "templateId", "d-template-id-123");
    }

    @Test
    void debeEnviarCorreoFlujoPlantillaDinamica() throws IOException {
        // ARRANGE
        String destinatario = "usuario@correo.com";
        String asunto = "Nuevo avistamiento de mascota"; // Activa el primer IF
        String cuerpoMensaje = "Alguien vio a tu mascota cerca de la plaza.";

        Response responseFake = new Response();
        responseFake.setStatusCode(202);
        responseFake.setBody("Success");

        // Interceptamos la creación interna de 'new SendGrid'
        try (MockedConstruction<SendGrid> mocked = Mockito.mockConstruction(SendGrid.class,
                (mock, context) -> {
                    // Hacemos que cuando el servicio llame a sg.api(request) devuelva nuestro 202 exitoso
                    when(mock.api(any(Request.class))).thenReturn(responseFake);
                })) {

            // ACT
            assertDoesNotThrow(() -> {
                emailService.enviarCorreo(destinatario, asunto, cuerpoMensaje);
            });

            // ASSERT
            assertEquals(1, mocked.constructed().size(), "Debería haberse instanciado SendGrid una vez");
            SendGrid instance = mocked.constructed().get(0);
            verify(instance, times(1)).api(any(Request.class));
        }
    }

    @Test
    void debeEnviarCorreoFlujoTextoPlano() throws IOException {
        // ARRANGE
        String destinatario = "usuario@correo.com";
        String asunto = "Confirmación ordinaria"; // Activa el ELSE
        String cuerpoMensaje = "Bienvenido a la plataforma Sanos y Salvos.";

        Response responseFake = new Response();
        responseFake.setStatusCode(202);
        responseFake.setBody("Success");

        // Interceptamos también para el flujo plano para evitar llamadas reales a internet
        try (MockedConstruction<SendGrid> mocked = Mockito.mockConstruction(SendGrid.class,
                (mock, context) -> {
                    when(mock.api(any(Request.class))).thenReturn(responseFake);
                })) {

            // ACT
            assertDoesNotThrow(() -> {
                emailService.enviarCorreo(destinatario, asunto, cuerpoMensaje);
            });

            // ASSERT
            assertEquals(1, mocked.constructed().size());
            SendGrid instance = mocked.constructed().get(0);
            verify(instance, times(1)).api(any(Request.class));
        }
    }
}