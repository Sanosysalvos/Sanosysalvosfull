"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import {
  Camera,
  Search,
  MapPin,
  Heart,
  Map,
  Mail,
  ShieldCheck,
  Globe,
  MessageCircle,
  Share2,
  Loader2,
} from "lucide-react";
import { petService, PetResponseDTO } from "@/services/petService";
import Header from "../components/header";

// Mantenemos los mockMascotas como respaldo por si el servidor falla
const mockMascotas: PetResponseDTO[] = [
  {
    id: "1",
    nombre: "Max",
    estado: "PERDIDO",
    direccionTexto: "Santiago Centro",
    foto: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=800&q=80",
    especie: "PERRO",
    descripcion: "Perro labrador dorado, muy amigable.",
    fechaPerdida: new Date().toISOString(),
    userUid: "mock-user-1",
    color: "Dorado",
    edad: 3,
    latitud: -33.4489,
    longitud: -70.6693,
  },
  {
    id: "2",
    nombre: "Luna",
    estado: "PERDIDO",
    direccionTexto: "Providencia",
    foto: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=80",
    especie: "GATO",
    descripcion: "Gata blanca con manchas negras.",
    fechaPerdida: new Date().toISOString(),
    userUid: "mock-user-2",
    color: "Blanco y Negro",
    edad: 2,
    latitud: -33.426,
    longitud: -70.619,
  },
  {
    id: "3",
    nombre: "Rocky",
    estado: "PERDIDO",
    direccionTexto: "Maipú",
    foto: "https://images.unsplash.com/photo-1537151608804-ea2f1fa73a74?auto=format&fit=crop&w=800&q=80",
    especie: "PERRO",
    descripcion: "Perro mestizo color café.",
    fechaPerdida: new Date().toISOString(),
    userUid: "mock-user-3",
    color: "Café",
    edad: 5,
    latitud: -33.52,
    longitud: -70.76,
  },
  {
    id: "4",
    nombre: "Milo",
    estado: "PERDIDO",
    direccionTexto: "La Florida",
    foto: "https://images.unsplash.com/photo-1495360010541-f48722b34f7d?auto=format&fit=crop&w=800&q=80",
    especie: "GATO",
    descripcion: "Gato gris azul ruso.",
    fechaPerdida: new Date().toISOString(),
    userUid: "mock-user-4",
    color: "Gris",
    edad: 2,
    latitud: -33.53,
    longitud: -70.56,
  },
];

export default function HomePage() {
  const [mascotas, setMascotas] = useState<PetResponseDTO[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const cargarMascotas = async () => {
      try {
        setLoading(true);
        const datosReales = await petService.getAllPets();

        const casosActivos = datosReales.filter(
          (pet) => pet.estado !== "RECUPERADO",
        );
        setMascotas(casosActivos);

        setError(null);
      } catch (err) {
        console.error("Error cargando mascotas, usando mocks:", err);
        setMascotas(mockMascotas);
        setError(null); // No mostramos error si tenemos mocks
      } finally {
        setLoading(false);
      }
    };

    cargarMascotas();
  }, []);

  return (
    <main className="min-h-screen bg-slate-50 flex flex-col">
      {/* --- NAVEGACIÓN (HEADER) --- */}
      <Header />

      {/* --- SECCIÓN DE BIENVENIDA (HERO) --- */}
      <section className="bg-indigo-600 text-white py-16 px-4">
        <div className="max-w-3xl mx-auto text-center">
          <h1 className="text-4xl sm:text-5xl font-extrabold mb-6">
            Ayudemos a que vuelvan a casa
          </h1>
          <p className="text-lg sm:text-xl text-indigo-100 mb-8">
            Revisa los reportes recientes de mascotas perdidas y encontradas en
            tu comunidad.
          </p>
        </div>
      </section>

      {/* --- SECCIÓN DE TARJETAS (GRILLA DINÁMICA) --- */}
      <section className="max-w-7xl mx-auto px-4 py-12 w-full flex-grow">
        <h2 className="text-3xl font-bold text-slate-900 mb-8">
          Reportes Recientes Activos
        </h2>

        {/* VISTA DE CARGA (LOADING) */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <Loader2 className="w-10 h-10 text-indigo-600 animate-spin" />
            <p className="text-slate-500 font-medium">
              Buscando reportes en la base de datos...
            </p>
          </div>
        )}

        {/* VISTA DE ERROR */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-6 py-4 rounded-xl text-center max-w-xl mx-auto my-10 shadow-sm">
            <p className="font-semibold mb-1">⚠️ Ups, algo salió mal</p>
            <p className="text-sm opacity-90">{error}</p>
          </div>
        )}

        {/* VISTA SIN DATOS (EMPTY STATE) */}
        {!loading && !error && mascotas.length === 0 && (
          <div className="text-center py-20 bg-white rounded-2xl border border-dashed border-slate-300 max-w-xl mx-auto my-6">
            <p className="text-slate-500 font-medium text-lg mb-2">
              ¡Grandes noticias!
            </p>
            <p className="text-slate-400 text-sm px-6">
              No hay reportes de mascotas perdidas activos en este momento en la
              comunidad.
            </p>
          </div>
        )}

        {/* Contenedor de la grilla real - LIMITADO A 4 */}
        {!loading && !error && mascotas.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {mascotas.slice(0, 4).map((mascota) => (
              <Link
                key={mascota.id}
                href={`/mascota/${mascota.id}`}
                className="group bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden hover:shadow-md transition-all duration-200 flex flex-col"
              >
                {/* Contenedor de la Imagen */}
                <div className="relative h-56 w-full bg-slate-200 overflow-hidden">
                  <img
                    src={
                      mascota.foto ||
                      "https://images.unsplash.com/photo-1543536448-d209d2d13a1c?auto=format&fit=crop&w=800&q=80"
                    }
                    alt={`Foto de ${mascota.nombre}`}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />

                  {/* Estado dinámico desde el backend */}
                  <div
                    className={`absolute top-3 right-3 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider shadow-sm backdrop-blur-sm ${
                      mascota.estado === "PERDIDO"
                        ? "bg-red-500/90 text-white"
                        : "bg-amber-500/90 text-white"
                    }`}
                  >
                    {mascota.estado.toLowerCase()}
                  </div>
                </div>

                {/* Contenedor de la Información Real */}
                <div className="p-5 flex flex-col flex-grow">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="text-xl font-bold text-slate-800 line-clamp-1 capitalize">
                      {mascota.nombre}
                    </h3>
                    <span className="text-xs text-slate-400 font-medium px-2 py-1 bg-slate-100 rounded-md uppercase">
                      {mascota.especie}
                    </span>
                  </div>

                  <p className="text-slate-500 text-sm line-clamp-2 mt-1 flex-grow">
                    {mascota.descripcion}
                  </p>

                  <p className="text-slate-400 text-xs mt-3 flex items-center gap-1 font-medium">
                    📅 Perdido el:{" "}
                    {new Date(mascota.fechaPerdida).toLocaleDateString()}
                  </p>

                  {/* Pie de la tarjeta */}
                  <div className="mt-4 pt-4 border-t border-slate-100 flex justify-between items-center text-indigo-600 font-semibold text-sm group-hover:text-indigo-700">
                    Ver detalles completos
                    <span className="transform group-hover:translate-x-1 transition-transform">
                      →
                    </span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>

      {/* --- FOOTER --- */}
      <footer className="bg-slate-900 text-slate-300 pt-16 pb-8">
        <div className="max-w-7xl mx-auto px-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
            <div className="col-span-1 md:col-span-1">
              <div className="text-2xl font-bold text-white flex items-center gap-2 mb-4">
                <Heart className="fill-indigo-500 text-indigo-500" /> Sanos y
                Salvos
              </div>
              <p className="text-sm leading-relaxed text-slate-400">
                Plataforma dedicada a la protección y reencuentro de mascotas
                perdidas. Construyendo una comunidad más segura para nuestros
                mejores amigos.
              </p>
            </div>

            <div>
              <h4 className="text-white font-bold mb-6">Plataforma</h4>
              <ul className="space-y-4 text-sm">
                <li>
                  <Link
                    href="/explorar"
                    className="hover:text-white transition-colors"
                  >
                    Explorar mascotas
                  </Link>
                </li>
                <li>
                  <Link
                    href="/reportar"
                    className="hover:text-white transition-colors"
                  >
                    Reportar caso
                  </Link>
                </li>
                <li>
                  <Link
                    href="/blog"
                    className="hover:text-white transition-colors"
                  >
                    Consejos de seguridad
                  </Link>
                </li>
              </ul>
            </div>

            <div>
              <h4 className="text-white font-bold mb-6">Ayuda</h4>
              <ul className="space-y-4 text-sm">
                <li>
                  <Link
                    href="/faq"
                    className="hover:text-white transition-colors"
                  >
                    Preguntas frecuentes
                  </Link>
                </li>
                <li>
                  <Link
                    href="/contacto"
                    className="hover:text-white transition-colors"
                  >
                    Contacto
                  </Link>
                </li>
                <li>
                  <Link
                    href="/privacidad"
                    className="hover:text-white transition-colors"
                  >
                    Privacidad
                  </Link>
                </li>
              </ul>
            </div>

            <div>
              <h4 className="text-white font-bold mb-6">Comunidad</h4>
              <div className="flex gap-4 mb-6">
                <a
                  href="#"
                  className="bg-slate-800 p-3 rounded-xl hover:bg-indigo-600 hover:text-white transition-all"
                >
                  <Globe size={20} />
                </a>
                <a
                  href="#"
                  className="bg-slate-800 p-3 rounded-xl hover:bg-indigo-600 hover:text-white transition-all"
                >
                  <MessageCircle size={20} />
                </a>
                <a
                  href="#"
                  className="bg-slate-800 p-3 rounded-xl hover:bg-indigo-600 hover:text-white transition-all"
                >
                  <Share2 size={20} />
                </a>
              </div>
              <div className="flex items-center gap-2 text-sm text-slate-400">
                <Mail size={16} />
                hola@sanosysalvos.cl
              </div>
            </div>
          </div>

          <div className="pt-8 border-t border-slate-800 flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-xs text-slate-500">
              © {new Date().getFullYear()} Sanos y Salvos. Todos los derechos
              reservados.
            </p>
            <div className="flex items-center gap-2 text-xs text-emerald-500 bg-emerald-500/10 px-3 py-1 rounded-full">
              <ShieldCheck size={14} />
              Servidor Seguro
            </div>
          </div>
        </div>
      </footer>
    </main>
  );
}
