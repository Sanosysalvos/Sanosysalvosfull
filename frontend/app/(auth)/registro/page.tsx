"use client";

import React, { useState } from "react";
import Link from "next/link";
import {
  Heart,
  Mail,
  Lock,
  User,
  Phone,
  MapPin,
  CreditCard,
} from "lucide-react";
import { auth } from "@/lib/firebase";
import {
  createUserWithEmailAndPassword,
  updateProfile,
  deleteUser,
} from "firebase/auth";
import { useRouter } from "next/navigation";

export default function RegisterPage() {
  const router = useRouter();

  const [formData, setFormData] = useState({
    nombre: "",
    rut: "",
    email: "",
    celular: "",
    direccion: "",
    password: "",
    confirmPassword: "",
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const validateRut = (rut: string) => {
    const cleanRut = rut.replace(/\D/g, "");
    if (cleanRut.length < 8) return false;

    const body = cleanRut.slice(0, -1);
    const dv = cleanRut.slice(-1).toUpperCase();

    let sum = 0;
    let multiplier = 2;

    for (let i = body.length - 1; i >= 0; i--) {
      sum += parseInt(body[i]) * multiplier;
      multiplier = multiplier === 7 ? 2 : multiplier + 1;
    }

    const expectedDv = 11 - (sum % 11);
    const dvDigit =
      expectedDv === 11 ? "0" : expectedDv === 10 ? "K" : expectedDv.toString();

    return dv === dvDigit;
  };

  const formatRut = (value: string) => {
    const clean = value.replace(/\D/g, "");
    if (clean.length <= 1) return clean;

    const body = clean.slice(0, -1);
    const dv = clean.slice(-1).toUpperCase();

    const formattedBody = body
      .replace(/\B(?=(\d{3})+(?!\d))/g, ".")
      .replace(",", ".");

    return `${formattedBody}-${dv}`;
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (formData.nombre.trim().length < 3) {
      newErrors.nombre = "El nombre debe tener al menos 3 caracteres";
    } else if (!/^[a-zA-Z\sáéíóúÁÉÍÓÚñÑ]+$/.test(formData.nombre)) {
      newErrors.nombre = "El nombre solo puede contener letras y espacios";
    }

    if (!validateRut(formData.rut)) {
      newErrors.rut = "RUT chileno inválido";
    }

    if (!/^(\+569\d{8})$/.test(formData.celular)) {
      newErrors.celular = "Formato inválido. Use +569 seguido de 9 dígitos";
    }

    if (formData.password.length < 8) {
      newErrors.password = "La contraseña debe tener al menos 8 caracteres";
    }

    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = "Las contraseñas no coinciden";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setErrors({});

    if (!validateForm()) return;
    let createdUser = null;

    try {
      // 1. Crear el usuario en Firebase
      const userCredential = await createUserWithEmailAndPassword(
        auth,
        formData.email,
        formData.password,
      );
      createdUser = userCredential.user;

      // 2. Actualizar el perfil en Firebase
      await updateProfile(createdUser, { displayName: formData.nombre });

      // 3. Obtener el Token JWT
      const token = await createdUser.getIdToken();

      // 4. Enviar los datos a tu Spring Boot
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/users`, // ← /api/users
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            firebaseUid: createdUser.uid, // ← corregido
            nombre: formData.nombre,
            rut: formData.rut,
            email: formData.email,
            celular: formData.celular,
            direccionResidencia: formData.direccion,
          }),
        },
      );
      if (!response.ok) {
        let dbError = response.statusText;
        try {
          const errorBody = await response.json();
          dbError = errorBody?.message || JSON.stringify(errorBody);
        } catch {
          dbError = await response.text().catch(() => response.statusText);
        }
        throw new Error(`Error al guardar en la base de datos: ${dbError}`);
      }

      console.log("¡Usuario registrado con éxito!");
      router.push("/"); // Cambia esto a tu ruta de inicio real
    } catch (err: any) {
      console.error("Error en handleSubmit:", err);

      if (createdUser) {
        try {
          await deleteUser(createdUser);
          console.warn(
            "Se eliminó el usuario de Firebase tras el fallo en la base de datos.",
          );
        } catch (deleteErr) {
          console.error("No se pudo eliminar el usuario creado:", deleteErr);
        }
      }

      if (err?.code === "auth/email-already-in-use") {
        setError("Este correo ya está registrado.");
      } else if (err?.code === "auth/weak-password") {
        setError("La contraseña debe tener al menos 6 caracteres.");
      } else {
        setError(err?.message || "Ocurrió un error durante el registro.");
      }
    } finally {
      setIsLoading(false);
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
        <h2 className="text-3xl font-extrabold text-slate-900">
          Cree su cuenta
        </h2>
        <p className="mt-2 text-sm text-slate-600">
          ¿Ya tiene cuenta?{" "}
          <Link
            href="/login"
            className="font-medium text-indigo-600 hover:text-indigo-500"
          >
            Inicie sesión aquí
          </Link>
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10 border border-slate-100">
          {/* Mensaje de Error Visual */}
          {error && (
            <div className="mb-4 bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md text-sm text-center">
              {error}
            </div>
          )}

          <form className="space-y-5" onSubmit={handleSubmit}>
            {/* Campo Nombre */}
            <div>
              <label className="block text-sm font-medium text-slate-700">
                Nombre completo
              </label>
              <div className="mt-1 relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <User className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  required
                  type="text"
                  className={`block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 sm:text-sm ${
                    errors.nombre ? "border-red-500" : "border-slate-300"
                  }`}
                  placeholder="Juan Perez"
                  onChange={(e) =>
                    setFormData({ ...formData, nombre: e.target.value })
                  }
                />
                {errors.nombre && (
                  <p className="mt-1 text-xs text-red-500">{errors.nombre}</p>
                )}
              </div>
            </div>

            {/* Campo RUT */}
            <div>
              <label className="block text-sm font-medium text-slate-700">
                RUT
              </label>
              <div className="mt-1 relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <CreditCard className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  required
                  type="text"
                  className={`block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 sm:text-sm ${
                    errors.rut ? "border-red-500" : "border-slate-300"
                  }`}
                  placeholder="12.345.678-9"
                  value={formData.rut}
                  onChange={(e) =>
                    setFormData({ ...formData, rut: formatRut(e.target.value) })
                  }
                />
                {errors.rut && (
                  <p className="mt-1 text-xs text-red-500">{errors.rut}</p>
                )}
              </div>
            </div>

            {/* Campo Email */}
            <div>
              <label className="block text-sm font-medium text-slate-700">
                Correo electrónico
              </label>
              <div className="mt-1 relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  required
                  type="email"
                  className="block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 sm:text-sm"
                  placeholder="usuario@ejemplo.com"
                  onChange={(e) =>
                    setFormData({ ...formData, email: e.target.value })
                  }
                />
              </div>
            </div>

            {/* Campo Celular */}
            <div>
              <label className="block text-sm font-medium text-slate-700">
                Celular
              </label>
              <div className="mt-1 relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Phone className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  required
                  type="tel"
                  className={`block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 sm:text-sm ${
                    errors.celular ? "border-red-500" : "border-slate-300"
                  }`}
                  placeholder="+569 1234 5678"
                  onChange={(e) =>
                    setFormData({ ...formData, celular: e.target.value })
                  }
                />
                {errors.celular && (
                  <p className="mt-1 text-xs text-red-500">{errors.celular}</p>
                )}
              </div>
            </div>

            {/* Campo Dirección */}
            <div>
              <label className="block text-sm font-medium text-slate-700">
                Dirección de residencia
              </label>
              <div className="mt-1 relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <MapPin className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  required
                  type="text"
                  className="block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 sm:text-sm"
                  placeholder="Av. Siempre Viva 123"
                  onChange={(e) =>
                    setFormData({ ...formData, direccion: e.target.value })
                  }
                />
              </div>
            </div>

            {/* Campo Password */}
            <div>
              <label className="block text-sm font-medium text-slate-700">
                Contraseña
              </label>
              <div className="mt-1 relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  required
                  type="password"
                  className={`block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 sm:text-sm ${
                    errors.password ? "border-red-500" : "border-slate-300"
                  }`}
                  onChange={(e) =>
                    setFormData({ ...formData, password: e.target.value })
                  }
                />
                {errors.password && (
                  <p className="mt-1 text-xs text-red-500">{errors.password}</p>
                )}
              </div>
            </div>

            {/* Confirmar Password */}
            <div>
              <label className="block text-sm font-medium text-slate-700">
                Confirmar contraseña
              </label>
              <div className="mt-1 relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-5 w-5 text-slate-400" />
                </div>
                <input
                  required
                  type="password"
                  className={`block w-full pl-10 pr-3 py-2 bg-white text-slate-900 placeholder:text-slate-400 border rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 sm:text-sm ${
                    errors.confirmPassword
                      ? "border-red-500"
                      : "border-slate-300"
                  }`}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      confirmPassword: e.target.value,
                    })
                  }
                />
                {errors.confirmPassword && (
                  <p className="mt-1 text-xs text-red-500">
                    {errors.confirmPassword}
                  </p>
                )}
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 ${
                isLoading
                  ? "bg-indigo-400 cursor-not-allowed"
                  : "bg-indigo-600 hover:bg-indigo-700"
              }`}
            >
              {isLoading ? "Creando cuenta..." : "Crear cuenta"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
