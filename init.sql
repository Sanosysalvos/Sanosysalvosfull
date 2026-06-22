-- 0. Habilitar extensión para UUIDs
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Tabla de Usuarios (Microservicio ms-users)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid VARCHAR(128) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    rut VARCHAR(12) UNIQUE,
    email VARCHAR(100) UNIQUE NOT NULL,
    celular VARCHAR(20),
    direccion_residencia TEXT,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabla de Mascotas (Microservicio ms-pets)
CREATE TABLE IF NOT EXISTS pets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    nombre VARCHAR(50) NOT NULL,
    especie VARCHAR(30) NOT NULL,
    color VARCHAR(30) NOT NULL,
    edad VARCHAR(20),
    descripcion TEXT,
    fecha_perdida DATE,
    estado VARCHAR(20) DEFAULT 'PERDIDO',
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8),
    direccion_texto TEXT,
    foto TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabla de Fotos
CREATE TABLE IF NOT EXISTS pet_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet_id UUID REFERENCES pets(id) ON DELETE CASCADE,
    photo_url TEXT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Tabla de Ubicaciones
CREATE TABLE IF NOT EXISTS locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descripcion_lugar TEXT,
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8),
    ciudad_comuna VARCHAR(100)
);

-- 6. Historial de Avistamientos (MODIFICADO PARA FLUJO PREDICTIVO)
CREATE TABLE IF NOT EXISTS pet_sightings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet_id UUID REFERENCES pets(id) ON DELETE SET NULL, -- Permite NULL si el usuario no sabe qué mascota es
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8),
    fecha_avistamiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comentario_adicional TEXT,
    photo_url TEXT
);