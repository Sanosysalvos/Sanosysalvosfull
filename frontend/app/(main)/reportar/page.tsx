"use client";

import React, { useState, useRef } from "react";
import Link from "next/link";
import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import {
  ArrowLeft,
  Calendar,
  AlignLeft,
  UploadCloud,
  Loader2,
  Info,
  Award,
  User,
  MapPin,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { CldUploadWidget } from "next-cloudinary";

const MapSelector = dynamic(
  () => import("@/app/(main)/components/mapSelector/page"),
  {
    ssr: false,
    loading: () => (
      <div className="w-full h-[300px] bg-slate-100 rounded-xl flex items-center justify-center">
        <Loader2 className="animate-spin text-indigo-500" size={32} />
      </div>
    ),
  },
);

const LAT_INICIAL = -33.4489;
const LNG_INICIAL = -70.6693;

export default function ReportarPage() {
  const router = useRouter();
  const { user } = useAuth();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [exito, setExito] = useState(false);
  const [fotoUrl, setFotoUrl] = useState("");
  const [latitud, setLatitud] = useState(LAT_INICIAL);
  const [longitud, setLongitud] = useState(LNG_INICIAL);
  const [direccionFormateada, setDireccionFormateada] = useState("");
  const [busquedaDireccion, setBusquedaDireccion] = useState("");
  const [sugerencias, setSugerencias] = useState<any[]>([]);
  const debounceRef = useRef<NodeJS.Timeout | null>(null);

  const [formData, setFormData] = useState({
    nombre: "",
    especie: "PERRO",
    fechaPerdida: "",
    descripcion: "",
    color: "",
    edad: "",
  });

  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >,
  ) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
  };

  const handleLocationChange = (
    lat: number,
    lng: number,
    direccion: string,
  ) => {
    setLatitud(lat);
    setLongitud(lng);
    setDireccionFormateada(direccion);
  };

  const handleBusquedaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const texto = e.target.value;
    setBusquedaDireccion(texto);

    if (debounceRef.current) clearTimeout(debounceRef.current);

    if (texto.length < 3) {
      setSugerencias([]);
      return;
    }

    debounceRef.current = setTimeout(async () => {
      try {
        const res = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(texto)}&limit=5&countrycodes=cl`,
        );
        const data = await res.json();
        setSugerencias(data);
      } catch {
        setSugerencias([]);
      }
    }, 400);
  };

  const seleccionarSugerencia = (sugerencia: any) => {
    setLatitud(parseFloat(sugerencia.lat));
    setLongitud(parseFloat(sugerencia.lon));
    setDireccionFormateada(sugerencia.display_name);
    setBusquedaDireccion(sugerencia.display_name);
    setSugerencias([]);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!user) {
      setError("Debes iniciar sesión para reportar una mascota.");
      return;
    }

    if (!fotoUrl) {
      setError("Por favor sube una foto de la mascota antes de publicar.");
      return;
    }

    if (!direccionFormateada && latitud === LAT_INICIAL) {
      setError("Por favor selecciona la ubicación en el mapa.");
      return;
    }

    setLoading(true);
    setError(null);

    const payload = {
      nombre: formData.nombre,
      especie: formData.especie.toUpperCase(),
      estado: "PERDIDO",
      fechaPerdida: formData.fechaPerdida,
      descripcion: formData.descripcion,
      color: formData.color || "No especificado",
      edad: formData.edad ? Number(formData.edad) : 0,
      foto: fotoUrl,
      userUid: user.uid,
      latitud,
      longitud,
      direccionFormateada,
    };

    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/reportar`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        },
      );

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || "Error al guardar el reporte");
      }

      setExito(true);
      setTimeout(() => router.push("/"), 2000);
    } catch (err: any) {
      setError(err.message || "Hubo un problema al guardar el reporte.");
    } finally {
      setLoading(false);
    }
  };

  const inputClasses =
    "block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors";

  return (
    <div className="min-h-screen bg-slate-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <div className="mb-8">
          <Link
            href="/"
            className="inline-flex items-center text-sm font-medium text-slate-500 hover:text-indigo-600 transition-colors"
          >
            <ArrowLeft className="mr-2 h-4 w-4" />
            Volver al inicio
          </Link>
        </div>

        <div className="text-center mb-10">
          <h1 className="text-3xl font-extrabold text-slate-900 sm:text-4xl">
            Reportar un caso
          </h1>
          <p className="mt-4 text-lg text-slate-600">
            {user
              ? `Hola ${user.displayName || "amigo"}, ayuda a una mascota hoy.`
              : "Inicia sesión para reportar una mascota."}
          </p>
        </div>

        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl text-center font-medium">
            ⚠️ {error}
          </div>
        )}

        {exito && (
          <div className="mb-6 bg-emerald-50 border border-emerald-200 text-emerald-700 px-4 py-4 rounded-xl text-center font-bold shadow-sm animate-pulse">
            🎉 ¡Reporte publicado con éxito! Redirigiendo al inicio...
          </div>
        )}

        <div className="bg-white py-8 px-6 shadow-sm rounded-2xl border border-slate-100 sm:px-10">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {/* Foto */}
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">
                Foto de la mascota
              </label>
              <CldUploadWidget
                uploadPreset="mascotas_preset"
                onSuccess={(results: any) =>
                  setFotoUrl(results.info.secure_url)
                }
              >
                {({ open }) => (
                  <button
                    type="button"
                    onClick={() => open()}
                    className={`w-full flex flex-col items-center justify-center py-8 border-2 border-dashed rounded-xl transition-all ${
                      fotoUrl
                        ? "border-emerald-500 bg-emerald-50"
                        : "border-slate-300 hover:border-indigo-500 bg-slate-50"
                    }`}
                  >
                    <UploadCloud
                      className={`h-10 w-10 mb-2 ${fotoUrl ? "text-emerald-500" : "text-slate-400"}`}
                    />
                    <span className="text-sm font-medium text-slate-600">
                      {fotoUrl ? "✓ Imagen cargada" : "Subir foto"}
                    </span>
                  </button>
                )}
              </CldUploadWidget>
            </div>

            {/* Nombre */}
            <div>
              <label
                htmlFor="nombre"
                className="block text-sm font-medium text-slate-700 mb-1"
              >
                Nombre de la mascota
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <User className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  id="nombre"
                  type="text"
                  value={formData.nombre}
                  onChange={handleChange}
                  className={inputClasses}
                  placeholder="Ej. Firulais"
                  required
                />
              </div>
            </div>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <label
                  htmlFor="especie"
                  className="block text-sm font-medium text-slate-700 mb-1"
                >
                  Especie
                </label>
                <select
                  id="especie"
                  value={formData.especie}
                  onChange={handleChange}
                  className="block w-full px-3 py-2 bg-white text-slate-900 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  <option value="PERRO">Perro</option>
                  <option value="GATO">Gato</option>
                  <option value="AVE">Ave</option>
                  <option value="OTRO">Otro</option>
                </select>
              </div>

              <div>
                <label
                  htmlFor="fechaPerdida"
                  className="block text-sm font-medium text-slate-700 mb-1"
                >
                  ¿Cuándo se perdió?
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Calendar className="h-5 w-5 text-slate-400" />
                  </div>
                  <input
                    id="fechaPerdida"
                    type="date"
                    value={formData.fechaPerdida}
                    onChange={handleChange}
                    max={new Date().toLocaleDateString("en-CA")}
                    className={inputClasses}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <label
                  htmlFor="color"
                  className="block text-sm font-medium text-slate-700 mb-1"
                >
                  Color o rasgos característicos
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Award className="h-5 w-5 text-slate-400" />
                  </div>
                  <input
                    id="color"
                    type="text"
                    value={formData.color}
                    onChange={handleChange}
                    placeholder="Ej. Negro con manchas blancas"
                    className={inputClasses}
                  />
                </div>
              </div>

              <div>
                <label
                  htmlFor="edad"
                  className="block text-sm font-medium text-slate-700 mb-1"
                >
                  Edad aproximada (años)
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <Info className="h-5 w-5 text-slate-400" />
                  </div>
                  <input
                    id="edad"
                    type="number"
                    min="0"
                    max="30"
                    value={formData.edad}
                    onChange={handleChange}
                    placeholder="Ej. 3"
                    className={inputClasses}
                  />
                </div>
              </div>
            </div>

            {/* Descripción */}
            <div>
              <label
                htmlFor="descripcion"
                className="block text-sm font-medium text-slate-700 mb-1"
              >
                Detalles del suceso
              </label>
              <div className="relative">
                <div className="absolute top-3 left-3 flex items-start pointer-events-none">
                  <AlignLeft className="h-5 w-5 text-slate-400" />
                </div>
                <textarea
                  id="descripcion"
                  rows={4}
                  value={formData.descripcion}
                  onChange={handleChange}
                  className="block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="Tenía collar rojo, cojea un poco, visto por última vez en..."
                  required
                />
              </div>
            </div>

            {/* MAPA */}
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">
                <MapPin className="inline mr-1 text-indigo-500" size={16} />
                Ubicación donde se perdió / encontró
              </label>
              <p className="text-xs text-slate-400 mb-3">
                Busca una dirección o haz clic en el mapa para ajustar la
                ubicación exacta.
              </p>

              {/* Buscador con texto predictivo */}
              <div className="relative mb-3">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <MapPin className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  type="text"
                  placeholder="Busca una dirección... Ej: Av. Providencia 1234, Santiago"
                  className="block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  value={busquedaDireccion}
                  onChange={handleBusquedaChange}
                  onKeyDown={(e) => e.key === "Escape" && setSugerencias([])}
                  autoComplete="off"
                />
                {/* Dropdown de sugerencias */}
                {sugerencias.length > 0 && (
                  <ul className="absolute z-50 w-full bg-white border border-slate-200 rounded-xl shadow-lg mt-1 overflow-hidden">
                    {sugerencias.map((s, i) => (
                      <li
                        key={i}
                        onClick={() => seleccionarSugerencia(s)}
                        className="flex items-start gap-2 px-4 py-3 hover:bg-indigo-50 cursor-pointer text-sm text-slate-700 border-b border-slate-100 last:border-0 transition-colors"
                      >
                        <MapPin
                          size={14}
                          className="text-indigo-400 mt-0.5 flex-shrink-0"
                        />
                        <span>{s.display_name}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <MapSelector
                latitud={latitud}
                longitud={longitud}
                onLocationChange={handleLocationChange}
                height="300px"
              />
              {direccionFormateada && (
                <div className="mt-2 flex items-center gap-2 text-sm text-slate-600 bg-indigo-50 px-3 py-2 rounded-lg border border-indigo-100">
                  <MapPin size={14} className="text-indigo-500 flex-shrink-0" />
                  <span>{direccionFormateada}</span>
                </div>
              )}
            </div>

            {/* Botón enviar */}
            <div className="pt-4">
              <button
                type="submit"
                disabled={loading || exito || !user}
                className="w-full flex justify-center items-center gap-2 py-3 px-4 border border-transparent rounded-xl shadow-sm text-base font-bold text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 transition-colors"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    Procesando reporte...
                  </>
                ) : (
                  "Publicar Mascota Perdida"
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
