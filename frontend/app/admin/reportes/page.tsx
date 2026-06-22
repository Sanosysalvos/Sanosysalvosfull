"use client";

import React, { useEffect, useState } from 'react';
import { Loader2, AlertCircle } from 'lucide-react';
import { petService, PetResponseDTO } from '@/services/petService';

export default function GestionReportes() {
  const [reportes, setReportes] = useState<PetResponseDTO[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Cargar los datos reales desde el backend
  useEffect(() => {
    const cargarReportes = async () => {
      try {
        setLoading(true);
        const data = await petService.getAllPets();
        // Ordenamos por fecha de pérdida (más recientes primero) opcional
        const dataOrdenada = data.sort((a, b) => 
          new Date(b.fechaPerdida).getTime() - new Date(a.fechaPerdida).getTime()
        );
        setReportes(dataOrdenada);
        setError(null);
      } catch (err) {
        console.error("Error al cargar los reportes:", err);
        setError("No se pudieron cargar los reportes del servidor.");
      } finally {
        setLoading(false);
      }
    };

    cargarReportes();
  }, []);

  // Función visual para simular eliminación (Temporal)
  const eliminarReporte = (id: string) => {
    if(confirm("¿Estás seguro de que deseas eliminar este reporte? (Aún no conectado al backend)")) {
      setReportes(reportes.filter(r => r.id !== id));
    }
  };

  // Función visual para simular cambio de estado (Temporal)
  const cambiarEstado = (id: string) => {
    alert("Función en desarrollo. Necesitamos crear un endpoint PUT en el backend.");
  };

  // Helper para asignar colores según el estado real
  const getColorPorEstado = (estado: string) => {
    switch (estado) {
      case 'PERDIDO': return 'bg-indigo-100 text-indigo-700';
      case 'AVISTADO': return 'bg-amber-100 text-amber-700';
      case 'RECUPERADO': return 'bg-emerald-100 text-emerald-700';
      case 'RETIRADO': return 'bg-slate-200 text-slate-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  // --- VISTAS DE CARGA Y ERROR ---
  if (loading) {
    return (
      <div className="p-8 flex flex-col items-center justify-center min-h-[60vh] gap-3">
        <Loader2 className="w-10 h-10 text-indigo-600 animate-spin" />
        <p className="text-slate-500 font-medium">Cargando lista de reportes...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8">
        <div className="bg-red-50 text-red-600 p-6 rounded-xl flex items-center gap-3">
          <AlertCircle className="w-6 h-6" />
          <span className="font-semibold">{error}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-slate-800">Gestión de Reportes</h1>
        <span className="text-slate-500 font-medium">{reportes.length} reportes listados</span>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        {reportes.length === 0 ? (
          <div className="p-8 text-center text-slate-500 italic">
            No hay reportes de mascotas en la base de datos.
          </div>
        ) : (
          <table className="w-full text-left">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Mascota</th>
                <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Estado</th>
                <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider">Fecha</th>
                <th className="px-6 py-4 text-xs font-bold text-slate-400 uppercase tracking-wider text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {reportes.map((reporte) => (
                <tr key={reporte.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-6 py-4">
                    <p className="font-bold text-slate-800">{reporte.nombre || 'Sin nombre'}</p>
                    <p className="text-xs text-slate-400 uppercase">{reporte.especie}</p>
                  </td>
                  <td className="px-6 py-4">
                    <span className={`px-3 py-1 rounded-full text-xs font-bold ${getColorPorEstado(reporte.estado)}`}>
                      {reporte.estado}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-slate-500 text-sm">
                    {new Date(reporte.fechaPerdida).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 text-right space-x-2">
                    <button 
                      onClick={() => cambiarEstado(reporte.id)}
                      className="text-xs bg-indigo-50 text-indigo-600 px-3 py-1.5 rounded-lg font-bold hover:bg-indigo-100 transition-colors"
                    >
                      Cambiar Estado
                    </button>
                    <button 
                      onClick={() => eliminarReporte(reporte.id)}
                      className="text-xs bg-red-50 text-red-600 px-3 py-1.5 rounded-lg font-bold hover:bg-red-100 transition-colors"
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}