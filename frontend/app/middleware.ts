import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  // 1. Buscamos el token de autenticación en las cookies del navegador
  // (Ajusta el nombre 'auth-token' al nombre real que uses)
  const token = request.cookies.get('auth-token')?.value;

  // 2. Definimos a dónde enviar al usuario si no está logueado
  const loginUrl = new URL('/login', request.url);

  // 3. Si no hay token, lo redirigimos al login
  if (!token) {
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
 
    '/admin/:path*',
    '/reportar/:path*',
    '/perfil/:path*'
  ],
};