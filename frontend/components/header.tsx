"use client";

import React from "react";
import Link from "next/link";
import { auth } from "@/lib/firebase";
import { signOut } from "firebase/auth";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
// Añadimos ShieldCheck para el icono de Admin
import { Heart, User, LogOut, ShieldCheck } from "lucide-react";

export default function Header() {
  const { user } = useAuth();
  const router = useRouter();

  const handleLogout = async () => {
    try {
      await signOut(auth);
      router.push("/");
    } catch (error) {
      console.error("Error al cerrar sesión:", error);
    }
  };

  return (
    <header className="bg-white border-b sticky top-0 z-50">
      <nav className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <Link
          href="/"
          className="text-2xl font-bold text-indigo-600 flex items-center gap-2 hover:opacity-90 transition"
        >
          <Heart className="fill-indigo-600 w-6 h-6" />
          <span>Sanos y Salvos</span>
        </Link>

        <div className="flex gap-4 items-center">
          <Link
            href="/explorar"
            className="text-slate-600 font-medium hover:text-indigo-600 px-3 py-2 transition-colors"
          >
            Avistamientos
          </Link>
          <Link
            href="/heatmap"
            className="text-slate-600 font-medium hover:text-indigo-600 px-3 py-2 transition-colors"
          >
            Mapa de Calor
          </Link>

          {user ? (
            <>
              {/* Usamos (user as any) para saltar la validación de tipos de Firebase */}
              {(user as any)?.isAdmin && (
                <Link
                  href="/admin"
                  className="flex items-center gap-1 text-amber-600 font-bold hover:text-amber-700 px-3 py-2 transition-colors border-r pr-4"
                >
                  <ShieldCheck size={20} /> Panel Admin
                </Link>
              )}

              <Link
                href="/reportar"
                className="bg-indigo-100 text-indigo-600 font-medium px-4 py-2 rounded-lg hover:bg-indigo-200 transition-colors"
              >
                Reportar Mascota
              </Link>

              <Link
                href="/perfil"
                className="text-slate-600 font-medium hover:text-indigo-600 px-3 py-2 transition-colors flex items-center gap-2"
              >
                <User size={20} /> Mi Perfil
              </Link>

              <button
                onClick={handleLogout}
                className="text-red-500 font-medium hover:text-red-700 px-3 py-2 transition-colors flex items-center gap-1"
              >
                <LogOut size={18} /> Salir
              </button>
            </>
          ) : (
            <Link
              href="/login"
              className="bg-indigo-600 text-white font-medium px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors shadow-sm"
            >
              Iniciar Sesión
            </Link>
          )}
        </div>
      </nav>
    </header>
  );
}
