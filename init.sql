-- ============================================================
-- SanoySalvos — Schema completo
-- Compatible con Docker PostgreSQL (imagen con PostGIS)
-- ============================================================

-- 0. Extensiones
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- ============================================================
-- 1. Usuarios (ms-users)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid         VARCHAR(128) UNIQUE NOT NULL,
    nombre               VARCHAR(100) NOT NULL,
    rut                  VARCHAR(12) UNIQUE,
    email                VARCHAR(100) UNIQUE NOT NULL,
    celular              VARCHAR(20),
    direccion_residencia TEXT,
    is_admin             BOOLEAN DEFAULT FALSE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. Mascotas (ms-pets)
-- Campos nuevos: tamanio
-- ============================================================
CREATE TABLE IF NOT EXISTS pets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    nombre          VARCHAR(50) NOT NULL,
    especie         VARCHAR(30) NOT NULL,   -- PERRO | GATO | AVE | OTRO
    color           VARCHAR(30) NOT NULL,   -- NEGRO | BLANCO | CAFE | GRIS | AMARILLO | ATIGRADO | MANCHADO | OTRO
    tamanio         VARCHAR(10),            -- PEQUEÑO | MEDIANO | GRANDE  ← NUEVO
    edad            VARCHAR(20),            -- CACHORRO | JOVEN | ADULTO | SENIOR
    descripcion     TEXT,
    fecha_perdida   DATE,
    estado          VARCHAR(20) DEFAULT 'PERDIDO',
    latitud         DECIMAL(10, 8),
    longitud        DECIMAL(11, 8),
    direccion_texto TEXT,
    foto            TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 3. Fotos de mascotas
-- ============================================================
CREATE TABLE IF NOT EXISTS pet_photos (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet_id     UUID REFERENCES pets(id) ON DELETE CASCADE,
    photo_url  TEXT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 4. Ubicaciones
-- ============================================================
CREATE TABLE IF NOT EXISTS locations (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descripcion_lugar TEXT,
    latitud          DECIMAL(10, 8),
    longitud         DECIMAL(11, 8),
    ciudad_comuna    VARCHAR(100)
);

-- ============================================================
-- 5. Avistamientos (ms-sightings)
-- Campos nuevos: reporter_uid, especie, color, tamanio, edad_aprox,
--                foto_url, porcentaje_coincidencia, notificacion_enviada,
--                fecha_avistamiento, location (PostGIS)
-- ============================================================
CREATE TABLE IF NOT EXISTS pet_sightings (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Relaciones
    pet_id                  UUID REFERENCES pets(id) ON DELETE SET NULL,  -- NULL en flujo predictivo
    user_id                 UUID REFERENCES users(id) ON DELETE SET NULL,
    reporter_uid            VARCHAR(128),                                  -- Firebase UID del reportante

    -- Ubicación
    latitud                 DECIMAL(10, 8),
    longitud                DECIMAL(11, 8),
    location                geography(Point, 4326),                       -- PostGIS para queries geoespaciales

    -- Descripción normalizada del animal avistado ← NUEVOS
    especie                 VARCHAR(20),   -- PERRO | GATO | AVE | OTRO
    color                   VARCHAR(30),   -- NEGRO | BLANCO | CAFE | GRIS | AMARILLO | ATIGRADO | MANCHADO | OTRO
    tamanio                 VARCHAR(10),   -- PEQUEÑO | MEDIANO | GRANDE
    edad_aprox              VARCHAR(15),   -- CACHORRO | JOVEN | ADULTO | SENIOR

    -- Texto libre adicional
    comentario              TEXT,
    foto_url                TEXT,

    -- Resultado del motor de matching (escrito por ms-matching)
    porcentaje_coincidencia DECIMAL(5, 2),
    notificacion_enviada    BOOLEAN DEFAULT FALSE,

    fecha_avistamiento      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Índices útiles para el motor de matching
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_sightings_pet_id   ON pet_sightings(pet_id);
CREATE INDEX IF NOT EXISTS idx_sightings_especie   ON pet_sightings(especie);
CREATE INDEX IF NOT EXISTS idx_pets_estado         ON pets(estado);
CREATE INDEX IF NOT EXISTS idx_pets_especie        ON pets(especie);