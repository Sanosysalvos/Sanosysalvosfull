"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { Loader2, MapPin } from "lucide-react";
import { petService, PetResponseDTO } from "@/services/petService";

export default function ExplorarPage() {
  // Estados para los datos del backend
  const [mascotas, setMascotas] = useState<PetResponseDTO[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Estados para filtrar la lista en tiempo real
  const [filtroTipo, setFiltroTipo] = useState<string>("Todos");
  const [filtroEstado, setFiltroEstado] = useState<string>("Todos");

  // Llamada al backend al cargar la página
  useEffect(() => {
    const obtenerMascotas = async () => {
      try {
        setLoading(true);
        const datos = await petService.getAllPets();
        setMascotas(datos);
        setError(null);
      } catch (err) {
        console.error("Error en explorar:", err);
        setError("No se pudo conectar con el servidor de reportes.");
      } finally {
        setLoading(false);
      }
    };
    obtenerMascotas();
  }, []);

  // Lógica de filtrado adaptada al DTO del backend
  const mascotasFiltradas = mascotas.filter((mascota) => {
    // El backend maneja 'PERRO' o 'GATO' en mayúsculas
    const coincideTipo =
      filtroTipo === "Todos" ||
      mascota.especie?.toUpperCase() === filtroTipo.toUpperCase();

    // El backend maneja 'PERDIDO', 'AVISTADO' o 'RECUPERADO'
    const coincideEstado =
      filtroEstado === "Todos" ||
      mascota.estado?.toUpperCase() === filtroEstado.toUpperCase();

    return coincideTipo && coincideEstado;
  });

  return (
    <main className="min-h-screen bg-slate-50 flex flex-col">
      {/* --- SECCIÓN DE FILTROS --- */}
      <section className="bg-white border-b py-8">
        <div className="max-w-7xl mx-auto px-4">
          <h1 className="text-3xl font-bold text-slate-900 mb-6">
            Explorar Reportes
          </h1>

          {/* Acciones rápidas */}
          <div className="flex flex-wrap gap-3 mb-8">
            <Link
              href="/reportar"
              className="inline-flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white font-bold rounded-xl shadow-sm hover:bg-indigo-700 transition-all"
            >
              Perdí mi mascota
            </Link>
            <Link
              href="/avistar"
              className="inline-flex items-center gap-2 px-6 py-3 bg-emerald-600 text-white font-bold rounded-xl shadow-sm hover:bg-emerald-700 transition-all"
            >
              Vi una mascota
            </Link>
          </div>

          <div className="flex flex-wrap gap-6">
            {/* Filtro por Tipo */}
            <div className="flex flex-col gap-2">
              <label className="text-sm font-semibold text-slate-500 uppercase">
                ¿Qué buscas?
              </label>
              <div className="flex gap-2">
                {["Todos", "Perro", "Gato"].map((tipo) => (
                  <button
                    key={tipo}
                    onClick={() => setFiltroTipo(tipo)}
                    className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                      filtroTipo === tipo
                        ? "bg-indigo-600 text-white shadow-md"
                        : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                    }`}
                  >
                    {tipo}
                  </button>
                ))}
              </div>
            </div>

            {/* Filtro por Estado (Sincronizado con Enums de Spring Boot) */}
            <div className="flex flex-col gap-2">
              <label className="text-sm font-semibold text-slate-500 uppercase">
                Estado
              </label>
              <div className="flex gap-2">
                {["Todos", "Perdido", "Avistado", "Recuperado"].map(
                  (estado) => (
                    <button
                      key={estado}
                      onClick={() => setFiltroEstado(estado)}
                      className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                        filtroEstado === estado
                          ? "bg-indigo-600 text-white shadow-md"
                          : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                      }`}
                    >
                      {estado}
                    </button>
                  ),
                )}
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* --- GRILLA DE RESULTADOS --- */}
      <section className="max-w-7xl mx-auto px-4 py-12 w-full flex-grow">
        {/* Manejo de estados de la conexión */}
        {loading && (
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <Loader2 className="w-10 h-10 text-indigo-600 animate-spin" />
            <p className="text-slate-500 font-medium">
              Sincronizando con el servidor...
            </p>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-6 py-4 rounded-xl text-center max-w-xl mx-auto my-10 shadow-sm">
            <p className="font-semibold">⚠️ {error}</p>
          </div>
        )}

        {!loading && !error && (
          <>
            <div className="flex justify-between items-center mb-8">
              <p className="text-slate-500 font-medium">
                Se encontraron{" "}
                <span className="text-indigo-600 font-bold">
                  {mascotasFiltradas.length}
                </span>{" "}
                mascotas
              </p>
            </div>

            {mascotasFiltradas.length === 0 ? (
              <div className="text-center py-20 bg-white rounded-2xl border-2 border-dashed border-slate-200 max-w-xl mx-auto">
                <p className="text-slate-400 text-lg">
                  No se encontraron mascotas con esos filtros.
                </p>
                <button
                  onClick={() => {
                    setFiltroTipo("Todos");
                    setFiltroEstado("Todos");
                  }}
                  className="mt-4 text-indigo-600 font-bold hover:underline"
                >
                  Limpiar filtros
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                {mascotasFiltradas.map((mascota) => (
                  <Link
                    key={mascota.id}
                    href={`/mascota/${mascota.id}`}
                    className="group bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden hover:shadow-md transition-all duration-200 flex flex-col"
                  >
                    <div className="relative h-52 w-full bg-slate-200 overflow-hidden">
                      <img
                        src={
                          mascota.foto ||
                          "https://images.unsplash.com/photo-1543536448-d209d2d13a1c?auto=format&fit=crop&w=800&q=80"
                        }
                        alt={mascota.nombre}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                      />
                      <div
                        className={`absolute top-3 right-3 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider shadow-sm ${
                          mascota.estado === "PERDIDO"
                            ? "bg-red-500/90 text-white"
                            : mascota.estado === "AVISTADO"
                              ? "bg-amber-500/90 text-white"
                              : "bg-emerald-500/90 text-white"
                        }`}
                      >
                        {mascota.estado?.toLowerCase() || "Desconocido"}
                      </div>
                    </div>

                    <div className="p-5 flex flex-col flex-grow">
                      <div className="flex justify-between items-start mb-2">
                        <h3 className="text-xl font-bold text-slate-800 capitalize">
                          {mascota.nombre || "Sin nombre"}
                        </h3>
                        <span className="text-xs text-slate-400 font-medium px-2 py-1 bg-slate-100 rounded-md uppercase">
                          {mascota.especie?.toLowerCase() || "Desconocido"}
                        </span>
                      </div>

                      {/* Mostramos la ubicación usando direccionTexto */}
                      <p className="text-slate-500 text-sm flex items-start gap-1.5 mt-1">
                        <MapPin
                          size={14}
                          className="text-indigo-500 flex-shrink-0 mt-0.5"
                        />
                        <span className="line-clamp-1">
                          {mascota.direccionTexto ||
                            "📍 Ubicación no especificada"}
                        </span>
                      </p>

                      <p className="text-slate-500 text-sm line-clamp-2 mt-2 flex-grow">
                        {mascota.descripcion || "Sin descripción"}
                      </p>

                      <div className="mt-auto pt-4 border-t border-slate-100 flex justify-between items-center text-indigo-600 font-semibold text-sm group-hover:text-indigo-700">
                        Ver detalle
                        <span className="transform group-hover:translate-x-1 transition-transform">
                          →
                        </span>
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </>
        )}
      </section>

      <footer className="bg-slate-900 text-slate-400 py-8 text-center text-sm mt-auto">
        <p>
          © {new Date().getFullYear()} Sanos y Salvos. Todos los derechos
          reservados.
        </p>
      </footer>
    </main>
  );
}
