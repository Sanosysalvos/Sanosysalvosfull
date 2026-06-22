"use client";

import React, { useEffect, useState } from 'react';
import { Loader2, AlertCircle } from 'lucide-react';
import { petService, PetResponseDTO } from '@/services/petService';

export default function AdminDashboard() {
  const [mascotas, setMascotas] = useState<PetResponseDTO[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const cargarDatos = async () => {
      try {
        setLoading(true);
        const data = await petService.getAllPets();
        setMascotas(data);
        setError(null);
      } catch (err) {
        console.error("Error al cargar datos del dashboard:", err);
        setError("No se pudieron cargar las estadísticas del servidor.");
      } finally {
        setLoading(false);
      }
    };

    cargarDatos();
  }, []);

  // --- VISTAS DE CARGA Y ERROR ---
  if (loading) {
    return (
      <div className="p-8 flex flex-col items-center justify-center min-h-[60vh] gap-3">
        <Loader2 className="w-10 h-10 text-indigo-600 animate-spin" />
        <p className="text-slate-500 font-medium">Calculando estadísticas...</p>
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

  // --- CÁLCULOS DINÁMICOS CON LOS DATOS REALES ---
  const totalReportes = mascotas.length;
  
  // 1. Conteo por estados exactos
  const perdidas = mascotas.filter(m => m.estado === 'PERDIDO').length;
  const avistadas = mascotas.filter(m => m.estado === 'AVISTADO').length;
  const recuperadas = mascotas.filter(m => m.estado === 'RECUPERADO').length;
  const retiradas = mascotas.filter(m => m.estado === 'RETIRADO').length; 
  
  const porcentajeRecuperadas = totalReportes === 0 ? 0 : Math.round((recuperadas / totalReportes) * 100);
  
  // 3. Cálculos precisos para el gráfico circular (Dona de 4 partes)
  const pctPerdidas = totalReportes === 0 ? 0 : (perdidas / totalReportes) * 100;
  const pctAvistadas = totalReportes === 0 ? 0 : (avistadas / totalReportes) * 100;
  const pctRecuperadas = totalReportes === 0 ? 0 : (recuperadas / totalReportes) * 100;
  // No necesitamos el de retiradas porque llenará el resto al 100%

  // 4. Reportes del mes actual
  const fechaActual = new Date();
  const mesActual = fechaActual.getMonth();
  const añoActual = fechaActual.getFullYear();
  
  const reportesDelMes = mascotas.filter(m => {
    const fechaMascota = new Date(m.fechaPerdida);
    return fechaMascota.getMonth() === mesActual && fechaMascota.getFullYear() === añoActual;
  }).length;

  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold text-slate-800 mb-8">Resumen del Sistema</h1>

      {/* Tarjetas de Estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
          <p className="text-sm font-bold text-slate-400 uppercase tracking-wider">Tasa de Recuperación</p>
          <p className="text-4xl font-black text-emerald-500 mt-2">{porcentajeRecuperadas}%</p>
        </div>
        
        <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
          <p className="text-sm font-bold text-slate-400 uppercase tracking-wider">Reportes Totales</p>
          <p className="text-4xl font-black text-slate-800 mt-2">{totalReportes}</p>
        </div>
        
        <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200">
          <p className="text-sm font-bold text-slate-400 uppercase tracking-wider">Nuevos este mes</p>
          <p className="text-4xl font-black text-indigo-600 mt-2">+{reportesDelMes}</p>
        </div>
      </div>

      {/* Visualización de Estados */}
      <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200">
        <h2 className="text-xl font-bold text-slate-800 mb-6">Distribución de Mascotas por Estado</h2>
        
        {totalReportes === 0 ? (
          <p className="text-slate-500 italic">No hay reportes suficientes para generar el gráfico.</p>
        ) : (
          <div className="flex flex-col sm:flex-row items-center gap-12">
            
            {/* Gráfico de dona de 4 colores */}
            <div className="relative w-56 h-56 rounded-full flex items-center justify-center bg-slate-100 shadow-inner"
                 style={{ 
                   background: `conic-gradient(
                     #4f46e5 0% ${pctPerdidas}%, 
                     #f59e0b ${pctPerdidas}% ${pctPerdidas + pctAvistadas}%, 
                     #10b981 ${pctPerdidas + pctAvistadas}% ${pctPerdidas + pctAvistadas + pctRecuperadas}%, 
                     #64748b ${pctPerdidas + pctAvistadas + pctRecuperadas}% 100%
                   )` 
                 }}
            >
              {/* Centro de la Dona */}
              <div className="absolute w-40 h-40 bg-white rounded-full flex items-center justify-center shadow-sm">
                 <div className="text-center">
                   <span className="text-4xl font-black text-slate-800">{totalReportes}</span>
                   <p className="text-xs font-bold text-slate-400 uppercase mt-1">Total</p>
                 </div>
              </div>
            </div>
            
            {/* Leyenda de datos */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-5 flex-1">
              {/* Perdidas */}
              <div className="flex items-center gap-4">
                <div className="w-5 h-5 bg-indigo-600 rounded-md shadow-sm"></div>
                <div>
                  <span className="text-slate-700 font-bold text-lg block">Perdidas</span>
                  <span className="text-slate-500 text-sm font-medium">{perdidas} mascota{perdidas !== 1 ? 's' : ''}</span>
                </div>
              </div>
              
              {/* Avistadas */}
              <div className="flex items-center gap-4">
                <div className="w-5 h-5 bg-amber-500 rounded-md shadow-sm"></div>
                <div>
                  <span className="text-slate-700 font-bold text-lg block">Avistadas</span>
                  <span className="text-slate-500 text-sm font-medium">{avistadas} mascota{avistadas !== 1 ? 's' : ''}</span>
                </div>
              </div>

              {/* Recuperadas */}
              <div className="flex items-center gap-4">
                <div className="w-5 h-5 bg-emerald-500 rounded-md shadow-sm"></div>
                <div>
                  <span className="text-slate-700 font-bold text-lg block">Recuperadas</span>
                  <span className="text-slate-500 text-sm font-medium">{recuperadas} mascota{recuperadas !== 1 ? 's' : ''}</span>
                </div>
              </div>

              {/* Retiradas */}
              <div className="flex items-center gap-4">
                <div className="w-5 h-5 bg-slate-500 rounded-md shadow-sm"></div>
                <div>
                  <span className="text-slate-700 font-bold text-lg block">Retiradas</span>
                  <span className="text-slate-500 text-sm font-medium">{retiradas} mascota{retiradas !== 1 ? 's' : ''}</span>
                </div>
              </div>
            </div>

          </div>
        )}
      </div>
    </div>
  );
}