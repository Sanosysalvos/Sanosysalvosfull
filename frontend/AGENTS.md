<!-- BEGIN:nextjs-agent-rules -->

# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.

<!-- END:nextjs-agent-rules -->

# Proyecto Sanos y Salvos

Ver PROYECTO.md para la arquitectura completa.

## Reglas

- Pedir confirmación antes de editar cualquier archivo
- No leer archivos fuera del módulo relevante al bug
- Microservicios en Java Spring Boot, frontend en Next.js 15
- Validaciones de RUT siempre con módulo 11 y K mayúscula
- No modificar estructura HTML ni clases Tailwind salvo que se pida explícitamente

## Stack

- Auth: Firebase
- Mapas: MapSelector con Nominatim
- Imágenes: Cloudinary
- API: BFF en Spring Boot → microservicios
