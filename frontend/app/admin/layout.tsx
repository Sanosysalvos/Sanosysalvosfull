// app/admin/layout.tsx
import React from 'react';
import Link from 'next/link';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-slate-100 flex">
      {/* Sidebar Fija */}
      <aside className="w-64 bg-slate-900 text-white flex flex-col sticky top-0 h-screen">
        <div className="p-6 border-b border-slate-800">
          <h2 className="text-xl font-bold text-indigo-400">Sanos y Salvos</h2>
          <p className="text-xs text-slate-400">Panel de Control</p>
        </div>
        <nav className="flex-1 p-4 space-y-2">
          <Link href="/admin" className="block px-4 py-2 hover:bg-slate-800 rounded-lg transition-colors">
            📊 Dashboard
          </Link>
          <Link href="/admin/reportes" className="block px-4 py-2 hover:bg-slate-800 rounded-lg transition-colors">
            📋 Gestionar Reportes
          </Link>
          <Link href="/admin/usuarios" className="block px-4 py-2 hover:bg-slate-800 rounded-lg transition-colors text-slate-500 cursor-not-allowed">
            👥 Usuarios (Próximamente)
          </Link>
        </nav>
        <div className="p-4 border-t border-slate-800">
          <Link href="/" className="text-sm text-slate-400 hover:text-white flex items-center gap-2">
            ← Volver a la App
          </Link>
        </div>
      </aside>

      {/* Contenido Dinámico */}
      <main className="flex-1 overflow-y-auto">
        {children}
      </main>
    </div>
  );
}