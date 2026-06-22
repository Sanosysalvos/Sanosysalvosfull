"use client";
import { toast } from "sonner";
import React, { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import dynamic from "next/dynamic";
import {
  ArrowLeft,
  MapPin,
  Calendar,
  FileText,
  Heart,
  Send,
  X,
  Eye,
} from "lucide-react";

const MapComponent = dynamic(
  () => import("@/app/(main)/components/mapComponent/page"),
  {
    ssr: false,
    loading: () => (
      <div className="h-[320px] bg-slate-100 rounded-[1.25rem] animate-pulse flex items-center justify-center text-slate-400 text-sm">
        Cargando mapa...
      </div>
    ),
  },
);

export default function DetalleMascota() {
  const { id } = useParams();
  const router = useRouter();
  const [mascota, setMascota] = useState<any>(null);
  const [cargando, setCargando] = useState(true);
  const [avistamientos, setAvistamientos] = useState<any[]>([]);

  // Modal contactar dueño
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [mensaje, setMensaje] = useState("");
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    const cargarDetalle = async () => {
      try {
        const baseUrl = process.env.NEXT_PUBLIC_API_URL;
        const res = await fetch(`${baseUrl}/api/explorar`, {
          cache: "no-store",
        });
        if (!res.ok) throw new Error("Error en servidor");
        const datos = await res.json();
        const encontrada = datos.find((m: any) => String(m.id) === String(id));

        console.log("Mascota encontrada:", {
          nombre: encontrada?.nombre,
          direccionTexto: encontrada?.direccionTexto,
        });

        setMascota(encontrada);
      } catch (error) {
        console.error("Error al cargar el detalle", error);
      } finally {
        setCargando(false);
      }
    };
    if (id) cargarDetalle();
  }, [id]);

  // Cargar avistamientos de esta mascota
  useEffect(() => {
    if (!id) return;
    const cargarAvistamientos = async () => {
      try {
        const res = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/api/avistar/mascota/${id}`,
        );
        if (res.ok) {
          const data = await res.json();
          console.log("Avistamientos cargados:", data);
          setAvistamientos(data);
        }
      } catch (error) {
        console.error("Error cargando avistamientos:", error);
      }
    };
    cargarAvistamientos();
  }, [id]);

  const handleEnviarMensaje = async (e: React.FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    try {
      const baseUrl = process.env.NEXT_PUBLIC_API_URL;
      const response = await fetch(`${baseUrl}/api/notificar-avistamiento`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ petId: id, mensaje }),
      });
      if (response.ok) {
        toast.success("¡Aviso enviado!", {
          description: `El dueño de ${mascota?.nombre} recibirá tu mensaje por correo.`,
        });
        setIsModalOpen(false);
        setMensaje("");
      } else {
        toast.error("Error al enviar", {
          description: "No pudimos contactar al dueño, intenta más tarde.",
        });
      }
    } catch {
      toast.error("Error de conexión");
    } finally {
      setEnviando(false);
    }
  };

  // Construir markers para el mapa
  const mapMarkers = React.useMemo(() => {
    const markers: any[] = [];

    // Marker de la mascota perdida
    if (mascota?.latitud && mascota?.longitud) {
      markers.push({
        lat: Number(mascota.latitud),
        lng: Number(mascota.longitud),
        type: "perdido",
        label: mascota.nombre,
        title: `📍 ${mascota.nombre} - Lugar donde se perdió`,
      });
    } else {
      console.warn("Mascota sin coordenadas:", mascota?.nombre);
    }

    // Markers de avistamientos
    avistamientos.forEach((av, index) => {
      if (av.latitud && av.longitud) {
        markers.push({
          lat: Number(av.latitud),
          lng: Number(av.longitud),
          type: "avistamiento",
          foto: av.fotoUrl,
          comentario: av.comentario,
          fecha: av.fechaAvistamiento || av.createdAt,
          title: `👁️ Avistamiento ${index + 1}: ${av.comentario || "Sin comentario"}`,
        });
      }
    });

    console.log("Markers generados:", markers);
    return markers;
  }, [mascota, avistamientos]);

  // Determinar centro del mapa
  const centerLat = mascota?.latitud ? Number(mascota.latitud) : -33.4489;
  const centerLng = mascota?.longitud ? Number(mascota.longitud) : -70.6693;

  if (cargando) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600" />
      </div>
    );
  }

  if (!mascota) {
    return (
      <div className="flex-grow flex flex-col items-center justify-center p-4 min-h-screen bg-slate-50">
        <h1 className="text-2xl font-bold text-slate-800">
          La mascota ya no existe
        </h1>
        <Link href="/" className="text-indigo-600 font-bold underline mt-2">
          Volver al inicio
        </Link>
      </div>
    );
  }

  return (
    <main className="min-h-screen bg-slate-50 flex flex-col relative">
      <div className="max-w-6xl mx-auto px-4 py-10 w-full">
        <button
          onClick={() => router.back()}
          className="flex items-center gap-2 text-slate-500 hover:text-indigo-600 mb-6 transition-colors font-medium"
        >
          <ArrowLeft size={18} /> Volver a la lista
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
          {/* Foto principal */}
          <div className="relative h-[420px] rounded-[32px] overflow-hidden shadow-2xl border-4 border-white">
            <img
              src={mascota.foto || "https://via.placeholder.com/800"}
              className="w-full h-full object-cover"
              alt={mascota.nombre}
            />
            <div
              className={`absolute top-6 right-6 px-4 py-2 rounded-full text-xs font-black uppercase tracking-widest text-white shadow-xl backdrop-blur-md ${
                mascota.estado?.toUpperCase() === "PERDIDO"
                  ? "bg-rose-600"
                  : "bg-emerald-500"
              }`}
            >
              {mascota.estado}
            </div>
          </div>

          {/* Info */}
          <div className="flex flex-col">
            <h1 className="text-4xl font-black text-slate-900 mb-2 uppercase italic tracking-tighter">
              {mascota.nombre}
            </h1>
            <span className="text-indigo-600 font-bold bg-indigo-50 px-4 py-1 rounded-full w-fit mb-6 text-sm">
              {mascota.especie || mascota.tipo}
            </span>

            <div className="space-y-4">
              {/* UBICACIÓN - USANDO SOLO direccionTexto */}
              <div className="flex items-center gap-4 bg-white p-4 rounded-2xl shadow-sm border border-slate-100">
                <div className="bg-red-50 p-3 rounded-xl text-red-500">
                  <MapPin size={20} />
                </div>
                <div>
                  <p className="text-xs font-bold text-slate-400 uppercase">
                    Ubicación
                  </p>
                  <p className="text-base font-semibold text-slate-800">
                    {mascota.direccionTexto || "📍 Ubicación no especificada"}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-4 bg-white p-4 rounded-2xl shadow-sm border border-slate-100">
                <div className="bg-amber-50 p-3 rounded-xl text-amber-500">
                  <Calendar size={20} />
                </div>
                <div>
                  <p className="text-xs font-bold text-slate-400 uppercase">
                    Fecha de Pérdida
                  </p>
                  <p className="text-base font-semibold text-slate-800">
                    {mascota.fechaPerdida
                      ? new Date(mascota.fechaPerdida).toLocaleDateString(
                          "es-CL",
                          {
                            day: "numeric",
                            month: "long",
                            year: "numeric",
                          },
                        )
                      : "No especificada"}
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-4 bg-white p-4 rounded-2xl shadow-sm border border-slate-100">
                <div className="bg-blue-50 p-3 rounded-xl text-blue-500 flex-shrink-0">
                  <FileText size={20} />
                </div>
                <div>
                  <p className="text-xs font-bold text-slate-400 uppercase">
                    Descripción
                  </p>
                  <p className="text-sm font-medium text-slate-700 leading-relaxed">
                    {mascota.descripcion || "Sin descripción disponible."}
                  </p>
                </div>
              </div>

              {/* Contador de avistamientos */}
              {avistamientos.length > 0 && (
                <div className="flex items-center gap-4 bg-sky-50 p-4 rounded-2xl border border-sky-100">
                  <div className="bg-sky-100 p-3 rounded-xl text-sky-500">
                    <Eye size={20} />
                  </div>
                  <div>
                    <p className="text-xs font-bold text-sky-400 uppercase">
                      Avistamientos
                    </p>
                    <p className="text-base font-semibold text-sky-800">
                      {avistamientos.length}{" "}
                      {avistamientos.length === 1
                        ? "avistamiento registrado"
                        : "avistamientos registrados"}
                    </p>
                  </div>
                </div>
              )}

              {/* Botones */}
              <div className="flex gap-3 pt-2">
                <button
                  onClick={() => setIsModalOpen(true)}
                  className="flex-grow bg-indigo-600 text-white font-black py-3.5 rounded-2xl hover:bg-indigo-700 transition-all uppercase tracking-widest text-xs flex items-center justify-center gap-2"
                >
                  <Send size={15} /> Contactar dueño
                </button>
                <Link
                  href={`/avistar?mascotaId=${id}`}
                  className="flex-grow bg-sky-500 text-white font-black py-3.5 rounded-2xl hover:bg-sky-600 transition-all uppercase tracking-widest text-xs flex items-center justify-center gap-2"
                >
                  <Eye size={15} /> Reportar avistamiento
                </Link>
                <button className="p-3.5 bg-white border border-slate-200 rounded-2xl text-slate-400 hover:text-red-500 transition-all shadow-sm">
                  <Heart size={20} />
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* MAPA */}
        <div className="mt-12 bg-white rounded-[2rem] border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-6 border-b border-slate-100">
            <h2 className="text-xl font-black text-slate-900 flex items-center gap-2">
              <MapPin size={20} className="text-indigo-500" />
              Mapa de ubicaciones
            </h2>
            <div className="flex items-center gap-6 mt-3 text-xs font-bold">
              <span className="flex items-center gap-1.5 text-red-500">
                <span className="w-3 h-3 rounded-full bg-red-500 inline-block" />
                Lugar de pérdida
              </span>
              {avistamientos.length > 0 && (
                <span className="flex items-center gap-1.5 text-sky-500">
                  <span className="w-3 h-3 rounded-full bg-sky-500 inline-block" />
                  Avistamientos ({avistamientos.length})
                </span>
              )}
            </div>
          </div>
          <div className="p-4">
            <MapComponent
              centerLat={centerLat}
              centerLng={centerLng}
              markers={mapMarkers}
              height="380px"
            />
          </div>
        </div>
      </div>

      {/* Modal contactar dueño */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
          <div className="bg-white w-full max-w-md rounded-[32px] shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
            <div className="p-8">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-black text-slate-900 uppercase italic">
                  Contactar Dueño
                </h2>
                <button
                  onClick={() => setIsModalOpen(false)}
                  className="text-slate-400 hover:text-slate-600"
                >
                  <X size={24} />
                </button>
              </div>
              <p className="text-slate-500 mb-6 text-sm">
                Envía un mensaje directo al dueño de{" "}
                <strong>{mascota?.nombre}</strong>. Se le notificará por correo.
              </p>
              <form onSubmit={handleEnviarMensaje}>
                <textarea
                  required
                  value={mensaje}
                  onChange={(e) => setMensaje(e.target.value)}
                  placeholder="Ej: Lo acabo de ver en la plaza, llevaba un collar azul..."
                  className="w-full h-32 p-4 bg-slate-50 border border-slate-200 rounded-2xl mb-6 focus:ring-2 focus:ring-indigo-600 focus:outline-none text-slate-700 resize-none"
                />
                <button
                  type="submit"
                  disabled={enviando}
                  className={`w-full py-4 rounded-2xl font-black uppercase tracking-widest text-xs flex items-center justify-center gap-2 transition-all ${
                    enviando
                      ? "bg-slate-200 text-slate-400"
                      : "bg-indigo-600 text-white hover:bg-indigo-700 shadow-lg shadow-indigo-200"
                  }`}
                >
                  {enviando ? (
                    "Enviando..."
                  ) : (
                    <>
                      <Send size={16} /> Enviar Aviso
                    </>
                  )}
                </button>
              </form>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
