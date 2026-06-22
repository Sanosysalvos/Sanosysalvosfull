# Proyecto Sanos y Salvos

Descripción general de la arquitectura del proyecto.

## Módulos

- [Frontend](./frontend/README.md) - Interfaz de usuario (Next.js).
- [BFF](./bff/README.md) - Capa de orquestación (Spring Boot).
- [MS Matching](./ms-matching/README.md) - Lógica de emparejamiento.
- [MS Notification](./ms-notification/README.md) - Gestión de notificaciones.
- [MS Pets](./ms-pets/README.md) - Gestión de mascotas.
- [MS Sightings](./ms-sightings/README.md) - Gestión de avistamientos.
- [MS Users](./ms-users/README.md) - Gestión de usuarios.

---

## 🌳 Estructura de Directorios

### 💻 Frontend
```text
frontend/
├── app/                # Rutas y páginas (App Router)
│   ├── (auth)/        # Rutas de autenticación
│   ├── (main)/        # Rutas principales de la aplicación
│   └── admin/         # Panel de administración
├── components/        # Componentes UI reutilizables (Header, Footer, etc.)
├── services/          # Clientes de API (petService.ts)
└── lib/              # Utilidades y configuraciones compartidas
```

### ⚙️ BFF (Backend For Frontend)
```text
bff/src/main/java/com/sanosysalvos/bff/bff/
├── BffApplication.java      # Punto de entrada de la aplicación
├── config/
│   ├── SecurityConfig.java     # Configuración de seguridad y CORS
│   └── SwaggerProxyController.java # Proxy para documentación de API
├── controller/
│   └── BffController.java      # Endpoints expuestos al frontend
├── services/
│   └── BffService.java         # Orquestación de llamadas a microservicios
└── model/
    └── PerfilResponse.java    # Modelos de respuesta para el frontend
```

### 🧩 Microservicios (Java Spring Boot)

#### MS Matching
```text
ms-matching/src/main/java/com/sanosysalvos/ms_matching/
├── MsMatchingApplication.java # Punto de entrada
├── controllers/
│   └── MatchingController.java # Gestión de solicitudes de matching
├── services/
│   └── MatchingService.java    # Lógica de emparejamiento de mascotas/avistamientos
└── dtos/
    └── MatchRequestDTO.java    # Objetos de transferencia de datos
```

#### MS Notification
```text
ms-notification/src/main/java/com/sanosysalvos/ms_notification/
├── MsNotificationApplication.java # Punto de entrada
├── controller/
│   └── NotificationController.java # Gestión de envío de alertas
└── service/
    └── EmailService.java           # Lógica de envío de correos electrónicos
```

#### MS Pets
```text
ms-pets/src/main/java/com/sanosysalvos/ms_pets/
├── MsPetsApplication.java # Punto de entrada
├── controllers/
│   └── PetController.java # Gestión de CRUD de mascotas
├── services/
│   └── PetService.java    # Lógica de negocio de mascotas
├── repositories/
│   └── PetRepository.java # Acceso a datos (Spring Data JPA)
├── models/
│   └── Pet.java           # Entidad de mascota
└── clients/
    └── UserClient.java    # Cliente para comunicar con MS Users
```

#### MS Sightings
```text
ms-sightings/src/main/java/com/sanosysalvos/ms_sightings/
├── MsSightingsApplication.java # Punto de entrada
├── controllers/
│   └── SightingController.java # Gestión de reportes de avistamientos
├── services/
│   └── SightingService.java    # Lógica de negocio de avistamientos
├── repositories/
│   └── SightingRepository.java # Acceso a datos de avistamientos
└── models/
    └── Sighting.java           # Entidad de avistamiento
```

#### MS Users
```text
ms-users/src/main/java/com/sanosysalvos/ms_users/
├── MsUsersApplication.java # Punto de entrada
├── controllers/
│   └── UserController.java # Gestión de perfiles de usuario
├── services/
│   └── UserService.java     # Lógica de negocio de usuarios
├── repositories/
│   └── UserRepository.java # Acceso a datos de usuarios
└── exceptions/
    └── GlobalExceptionHandler.java # Manejo centralizado de errores
```
