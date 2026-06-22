"use client";

import { useState } from "react";
import MapaCalor from "../components/MapaCalor";
import {
  Filter,
  Clock,
  PawPrint,
  TrendingUp,
  AlertTriangle,
  MapPin,
} from "lucide-react";

// Opciones para filtros
const OPCIONES_TIEMPO = [
  { valor: 24, label: "Últimas 24h", icono: Clock },
  { valor: 168, label: "Últimos 7 días", icono: Clock },
  { valor: 720, label: "Últimos 30 días", icono: Clock },
];

const OPCIONES_ESPECIE = [
  { valor: "todos", label: "Todos", icono: PawPrint },
  { valor: "PERRO", label: "Perros", icono: PawPrint }, // ← MAYÚSCULAS
  { valor: "GATO", label: "Gatos", icono: PawPrint }, // ← MAYÚSCULAS
];
// Top zonas estático (puede venir de API después)
const TOP_ZONAS = [
  { nombre: "Viña del Mar", cantidad: 8, incidencia: "alta" },
  { nombre: "Santiago Centro", cantidad: 5, incidencia: "media" },
  { nombre: "Valparaíso", cantidad: 4, incidencia: "media" },
];

export default function ExplorerPage() {
  const [horas, setHoras] = useState(168);
  const [especie, setEspecie] = useState("todos");
  const [totalMascotas, setTotalMascotas] = useState(0);
  const [cargando, setCargando] = useState(true);

  // Insight dinámico según filtros
  const obtenerInsight = () => {
    if (totalMascotas === 0) return "No hay mascotas perdidas en este período";
    if (horas === 24)
      return `${totalMascotas} mascotas perdidas en las últimas 24 horas. ¡Revisa tu zona!`;
    if (horas === 168)
      return `${totalMascotas} mascotas perdidas en la última semana. Mayor incidencia entre 18:00-20:00 hrs.`;
    return `${totalMascotas} mascotas perdidas en el último mes. Las zonas costeras concentran más reportes.`;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Barra superior con título */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="max-w-7xl mx-auto">
          <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
            <span className="text-3xl">🔥</span>
            Mapa de calor - Zonas críticas
          </h1>
          <p className="text-gray-500 text-sm mt-1">
            Áreas con mayor incidencia de mascotas perdidas
          </p>
        </div>
      </div>

      {/* Contenido principal con layout de dos columnas */}
      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="flex flex-col lg:flex-row gap-6">
          {/* PANEL LATERAL IZQUIERDO - FILTROS Y ESTADÍSTICAS */}
          <aside className="lg:w-80 flex-shrink-0 space-y-5">
            {/* Tarjeta de filtros */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
              <div className="flex items-center gap-2 mb-4 pb-2 border-b border-gray-100">
                <Filter size={18} className="text-indigo-500" />
                <h2 className="font-semibold text-gray-800">Filtros</h2>
              </div>

              {/* Filtro de tiempo */}
              <div className="mb-5">
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 block">
                  Período
                </label>
                <div className="grid grid-cols-3 gap-2">
                  {OPCIONES_TIEMPO.map((opcion) => (
                    <button
                      key={opcion.valor}
                      onClick={() => setHoras(opcion.valor)}
                      className={`px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                        horas === opcion.valor
                          ? "bg-indigo-600 text-white shadow-sm"
                          : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                      }`}
                    >
                      {opcion.label.split(" ")[1] || opcion.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Filtro de especie */}
              <div>
                <label className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 block">
                  Especie
                </label>
                <div className="flex gap-2">
                  {OPCIONES_ESPECIE.map((opcion) => (
                    <button
                      key={opcion.valor}
                      onClick={() => setEspecie(opcion.valor)}
                      className={`flex-1 px-4 py-2 rounded-lg text-sm font-medium transition-all flex items-center justify-center gap-1 ${
                        especie === opcion.valor
                          ? "bg-indigo-600 text-white shadow-sm"
                          : "bg-gray-100 text-gray-600 hover:bg-gray-200"
                      }`}
                    >
                      <opcion.icono size={14} />
                      {opcion.label}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            {/* Tarjeta de estadísticas */}
            <div className="bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl shadow-sm p-5 text-white">
              <div className="flex items-center justify-between mb-3">
                <span className="text-sm font-medium opacity-90">Total</span>
                <TrendingUp size={16} className="opacity-75" />
              </div>
              <div className="text-3xl font-bold mb-1">
                {cargando ? "..." : totalMascotas}
              </div>
              <p className="text-xs opacity-80">
                mascotas perdidas
                <br />
                en el período seleccionado
              </p>
            </div>

            {/* Top zonas críticas */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5">
              <div className="flex items-center gap-2 mb-4 pb-2 border-b border-gray-100">
                <AlertTriangle size={18} className="text-amber-500" />
                <h2 className="font-semibold text-gray-800">Zonas críticas</h2>
              </div>
              <div className="space-y-3">
                {TOP_ZONAS.map((zona, idx) => (
                  <div
                    key={zona.nombre}
                    className="flex items-center justify-between p-2 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
                  >
                    <div className="flex items-center gap-3">
                      <span className="text-sm font-bold text-gray-400 w-5">
                        {idx + 1}
                      </span>
                      <div>
                        <p className="font-medium text-gray-800 text-sm">
                          {zona.nombre}
                        </p>
                        <p className="text-xs text-gray-400">
                          {zona.cantidad} mascotas
                        </p>
                      </div>
                    </div>
                    <div
                      className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                        zona.incidencia === "alta"
                          ? "bg-red-100 text-red-600"
                          : "bg-yellow-100 text-yellow-600"
                      }`}
                    >
                      {zona.incidencia === "alta" ? "Alta" : "Media"}
                    </div>
                  </div>
                ))}
              </div>
              <button className="w-full mt-4 text-xs text-indigo-600 hover:text-indigo-700 font-medium flex items-center justify-center gap-1">
                <MapPin size={12} />
                Ver todas las zonas
              </button>
            </div>
          </aside>

          {/* MAPA - CENTRO */}
          <main className="flex-1">
            <MapaCalor
              horas={horas}
              especie={especie}
              onTotalChange={setTotalMascotas}
              onCargandoChange={setCargando}
            />
          </main>
        </div>

        {/* ZONA INFERIOR - INSIGHTS */}
        <div className="mt-6 bg-white rounded-xl shadow-sm border border-gray-200 p-4">
          <div className="flex items-start gap-3">
            <div className="bg-indigo-100 rounded-full p-2">
              <TrendingUp size={18} className="text-indigo-600" />
            </div>
            <div>
              <h3 className="font-semibold text-gray-800 text-sm">Insight</h3>
              <p className="text-gray-600 text-sm mt-0.5">{obtenerInsight()}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
