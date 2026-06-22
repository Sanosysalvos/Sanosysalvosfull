"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { onAuthStateChanged, User } from "firebase/auth";
import { auth } from "@/lib/firebase";

export interface CustomUser extends User {
  isAdmin?: boolean;
  // Añadimos tipado flexible para los datos que traes de tu backend (RUT, celular, etc.)
  dbData?: any;
}

interface AuthContextType {
  user: CustomUser | null;
  loading: boolean;
  refreshUser: () => Promise<void>; // ← NUEVA: Para avisarle al frontend que re-consulte la BD
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  loading: true,
  refreshUser: async () => {},
});

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<CustomUser | null>(null);
  const [loading, setLoading] = useState(true);

  // Función aislada para consultar al BFF/Backend el estado real en PostgreSQL
  const enrichUser = async (firebaseUser: User) => {
    try {
      // Obtenemos el token JWT real de Firebase para romper los 401 del Spring Boot
      const token = await firebaseUser.getIdToken();

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/usuarios/firebase/${firebaseUser.uid}`,
        {
          headers: {
            Authorization: `Bearer ${token}`, // ← Inyectamos el token
            "Content-Type": "application/json",
          },
        },
      );

      if (response.ok) {
        const dbData = await response.json();
        setUser({
          ...firebaseUser,
          isAdmin: dbData.is_admin || dbData.isAdmin || false,
          dbData: dbData, // Almacenamos toda la info (RUT, celular, etc.) en el estado global
        });
      } else {
        setUser(firebaseUser);
      }
    } catch (error) {
      console.error("Error al enriquecer usuario:", error);
      setUser(firebaseUser);
    }
  };

  const refreshUser = async () => {
    if (auth.currentUser) {
      await enrichUser(auth.currentUser);
    }
  };

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        await enrichUser(firebaseUser);
      } else {
        setUser(null);
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, refreshUser }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
