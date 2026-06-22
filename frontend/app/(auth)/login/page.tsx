"use client"; // Muy importante agregarlo porque usamos useState y useRouter

import React, { useState } from "react";
import Link from "next/link";
import { Heart, Mail, Lock } from "lucide-react";
import { useRouter } from "next/navigation";
import { auth } from "@/lib/firebase"; // Asegúrate de que esta ruta coincida con tu archivo firebase.ts
import { signInWithEmailAndPassword } from "firebase/auth";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(""); // Limpiamos errores previos

    try {
      // 1. Iniciamos sesión con Firebase
      const userCredential = await signInWithEmailAndPassword(
        auth,
        email,
        password,
      );
      const user = userCredential.user;

      // 2. Obtenemos el token de seguridad
      const token = await user.getIdToken();

      // 3. Buscamos/Sincronizamos al usuario en nuestro backend
      const response = await fetch("http://localhost:8081/api/users", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          firebaseUid: user.uid,
          email: user.email,
          nombre: user.displayName || "Usuario",
        }),
      });

      if (response.ok) {
        // 4. ¡Aquí está la magia! Leemos lo que nos responde Spring Boot
        const userData = await response.json();

        // Evaluamos el rol (Ojo: dependiendo de cómo lo devuelva tu Java, puede llamarse isAdmin, admin, o is_admin)
        if (
          userData.isAdmin === true ||
          userData.admin === true ||
          userData.is_admin === true
        ) {
          console.log("¡Bienvenido Administrador!");
          router.push("/admin"); // Lo mandamos a la vista de administrador
        } else {
          console.log("¡Bienvenido Usuario normal!");
          router.push("/"); // Lo mandamos a la vista normal
        }
      } else {
        setError("Error al obtener los datos del servidor.");
      }
    } catch (err: any) {
      setError("Credenciales incorrectas o error de conexión.");
      console.error(err.message);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <Link
          href="/"
          className="inline-flex items-center gap-2 text-2xl font-bold text-indigo-600 mb-6 hover:opacity-80 transition-opacity"
        >
          <Heart className="fill-current" /> Sanos y Salvos
        </Link>
        <h2 className="mt-2 text-center text-3xl font-extrabold text-slate-900">
          Inicie sesión en su cuenta
        </h2>
        <p className="mt-2 text-center text-sm text-slate-600">
          ¿No tiene una cuenta?{" "}
          <Link
            href="/registro"
            className="font-medium text-indigo-600 hover:text-indigo-500"
          >
            Regístrese aquí
          </Link>
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 border border-slate-100">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {/* --- ALERTA DE ERROR --- */}
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md text-sm font-medium text-center">
                {error}
              </div>
            )}

            <div>
              <label
                htmlFor="email"
                className="block text-sm font-medium text-slate-700"
              >
                Correo electrónico
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent sm:text-sm transition-colors"
                  placeholder="usuario@ejemplo.com"
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="password"
                className="block text-sm font-medium text-slate-700"
              >
                Contraseña
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="current-password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent sm:text-sm transition-colors"
                  placeholder="••••••••"
                />
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <input
                  id="remember-me"
                  name="remember-me"
                  type="checkbox"
                  className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-slate-300 rounded"
                />
                <label
                  htmlFor="remember-me"
                  className="ml-2 block text-sm text-slate-900"
                >
                  Recordarme
                </label>
              </div>

              <div className="text-sm">
                <Link
                  href="/recuperar-password"
                  className="font-medium text-indigo-600 hover:text-indigo-500"
                >
                  ¿Olvidó su contraseña?
                </Link>
              </div>
            </div>

            <div>
              <button
                type="submit"
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors"
              >
                Ingresar
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
