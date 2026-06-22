"use client";

import { useEffect, useRef, useState } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "leaflet.heat";

declare module "leaflet" {
  namespace HeatMap {
    interface HeatMapOptions {
      radius?: number;
      blur?: number;
      maxZoom?: number;
      minOpacity?: number;
      gradient?: Record<number, string>;
    }
  }

  function heatLayer(
    latlngs: Array<[number, number, number]>,
    options?: HeatMap.HeatMapOptions,
  ): any;
}

interface PuntoCalor {
  latitud: number;
  longitud: number;
  intensidad: number;
  nombre: string;
  especie: string;
}

interface MapaCalorProps {
  horas?: number;
  especie?: string;
  onTotalChange?: (total: number) => void;
  onCargandoChange?: (cargando: boolean) => void;
}

export default function MapaCalor({
  horas = 168,
  especie = "todos",
  onTotalChange,
  onCargandoChange,
}: MapaCalorProps) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<L.Map | null>(null);
  const heatLayerRef = useRef<any>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalMascotas, setTotalMascotas] = useState(0);

  // Notificar cambios de estado al padre
  useEffect(() => {
    onCargandoChange?.(cargando);
  }, [cargando, onCargandoChange]);

  useEffect(() => {
    onTotalChange?.(totalMascotas);
  }, [totalMascotas, onTotalChange]);

  // Función para cargar datos y dibujar heatmap
  const cargarYDibujar = async () => {
    if (!mapInstanceRef.current) return;

    try {
      setCargando(true);

      const url = `${process.env.NEXT_PUBLIC_API_URL}/api/heatmap?horas=${horas}${especie !== "todos" ? `&especie=${especie}` : ""}`;
      console.log(`Fetching heatmap: ${url}`);

      const res = await fetch(url);

      if (!res.ok) {
        throw new Error(`Error ${res.status}: ${res.statusText}`);
      }

      const data = await res.json();
      console.log("Respuesta del backend:", data);

      if (!data.success) {
        throw new Error(data.error || "Error al cargar datos");
      }

      setTotalMascotas(data.total || 0);
      const puntos = data.data || [];

      // Remover heatmap anterior si existe
      if (heatLayerRef.current && mapInstanceRef.current) {
        mapInstanceRef.current.removeLayer(heatLayerRef.current);
        heatLayerRef.current = null;
      }

      if (puntos && puntos.length > 0) {
        const puntosValidos = puntos.filter(
          (p: PuntoCalor) =>
            p.latitud &&
            p.longitud &&
            !isNaN(p.latitud) &&
            !isNaN(p.longitud) &&
            Math.abs(p.latitud) <= 90 &&
            Math.abs(p.longitud) <= 180,
        );

        if (puntosValidos.length === 0) {
          console.warn("⚠️ No hay puntos válidos después del filtrado");
          setCargando(false);
          return;
        }

        const heatData = puntosValidos.map((p: PuntoCalor) => [
          p.latitud,
          p.longitud,
          p.intensidad || 1.0,
        ]);

        console.log(`🔥 Generando heatmap con ${heatData.length} puntos`);

        if (typeof (L as any).heatLayer !== "function") {
          console.error("❌ leaflet.heat no está cargado correctamente");
          setError("La librería de mapa de calor no está disponible");
          setCargando(false);
          return;
        }

        try {
          const heatLayer = (L as any).heatLayer(heatData, {
            radius: 25,
            blur: 15,
            maxZoom: 17,
            minOpacity: 0.3,
            gradient: {
              0.2: "#2c7bb6",
              0.4: "#abd9e9",
              0.6: "#ffffbf",
              0.8: "#fdae61",
              1.0: "#d7191c",
            },
          });

          if (heatLayer && mapInstanceRef.current) {
            heatLayer.addTo(mapInstanceRef.current);
            heatLayerRef.current = heatLayer;
            console.log("✅ Heatmap agregado al mapa");

            // Ajustar zoom para mostrar todos los puntos
            if (heatData.length > 0) {
              const bounds = L.latLngBounds(
                heatData.map((h: any[]) => [h[0], h[1]]),
              );
              mapInstanceRef.current.fitBounds(bounds, { padding: [50, 50] });
            }
          }
        } catch (heatError) {
          console.error("❌ Error al crear heatLayer:", heatError);
          setError("Error al dibujar el mapa de calor");
        }
      } else {
        console.log("ℹ️ No hay datos de calor para mostrar");
      }
      setCargando(false);
    } catch (err) {
      console.error("❌ Error en la carga de datos:", err);
      setError("No se pudo cargar el mapa de calor");
      setCargando(false);
    }
  };

  // Inicializar mapa
  useEffect(() => {
    if (!mapRef.current) return;

    console.log("🗺️ Inicializando mapa...");

    if (mapInstanceRef.current) {
      mapInstanceRef.current.remove();
    }

    const map = L.map(mapRef.current).setView([-33.4489, -70.6693], 12);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "© OpenStreetMap contributors | SanosySalvos",
    }).addTo(map);

    mapInstanceRef.current = map;

    // Cargar datos iniciales
    cargarYDibujar();

    return () => {
      console.log("🗺️ Limpiando mapa...");
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, []); // Solo se ejecuta una vez

  // Recargar cuando cambian los filtros
  useEffect(() => {
    if (mapInstanceRef.current) {
      cargarYDibujar();
    }
  }, [horas, especie]);

  if (error) {
    return (
      <div className="w-full h-[600px] bg-gray-100 rounded-lg flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600 font-medium">❌ {error}</p>
          <p className="text-gray-500 text-sm mt-2">
            Intenta nuevamente más tarde
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="relative w-full h-[600px] rounded-lg overflow-hidden shadow-lg">
      {cargando && (
        <div className="absolute inset-0 bg-white/80 flex items-center justify-center z-10">
          <div className="text-center">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600 mx-auto"></div>
            <p className="mt-2 text-gray-600 font-medium">
              Cargando mapa de calor...
            </p>
          </div>
        </div>
      )}
      <div ref={mapRef} className="w-full h-full" />

      {/* Leyenda */}
      <div className="absolute bottom-4 right-4 bg-white/95 backdrop-blur-sm p-3 rounded-lg shadow-md text-sm z-[1000] border border-gray-200">
        <h4 className="font-semibold text-gray-900 mb-2">🔥 Zonas de calor</h4>
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-[#d7191c] rounded-sm shadow-sm"></div>
            <span className="text-gray-800 font-medium">Alta incidencia</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-[#fdae61] rounded-sm shadow-sm"></div>
            <span className="text-gray-800 font-medium">Media-Alta</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-[#ffffbf] rounded-sm shadow-sm"></div>
            <span className="text-gray-800 font-medium">Media</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 bg-[#2c7bb6] rounded-sm shadow-sm"></div>
            <span className="text-gray-800 font-medium">Baja incidencia</span>
          </div>
        </div>
        <div className="mt-2 pt-2 border-t border-gray-200 text-xs">
          <span className="text-gray-700 font-medium">
            {totalMascotas} mascotas perdidas (
            {horas === 24
              ? "último día"
              : horas === 168
                ? "últimos 7 días"
                : "últimos 30 días"}
            )
          </span>
        </div>
      </div>
    </div>
  );
}
