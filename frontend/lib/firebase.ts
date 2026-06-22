import { initializeApp, getApps, getApp } from "firebase/app";
import { getAuth } from "firebase/auth";

// 1. Tomamos las variables de entorno
const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
};
console.log("Firebase config:", {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY ? "OK" : "MISSING",
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN ? "OK" : "MISSING",
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID ? "OK" : "MISSING",
});

const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();

export const auth = getAuth(app);
