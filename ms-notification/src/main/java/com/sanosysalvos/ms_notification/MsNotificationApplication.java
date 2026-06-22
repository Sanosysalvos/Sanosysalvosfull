package com.sanosysalvos.ms_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // 👈 Importante importar la anotación

@SpringBootApplication
@EnableAsync // 👈 Con esto le das "luz verde" a Spring para usar @Async
public class MsNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsNotificationApplication.class, args);
    }

}