import React from "react";
import Link from "next/link";
import {
  Heart,
  Globe,
  MessageCircle,
  Share2,
  Mail,
  ShieldCheck,
} from "lucide-react";

export default function Footer() {
  return (
    <footer className="bg-slate-900 text-slate-300 pt-16 pb-8">
      <div className="max-w-7xl mx-auto px-4">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
          <div className="col-span-1 md:col-span-1">
            <div className="text-2xl font-bold text-white flex items-center gap-2 mb-4">
              <Heart className="fill-indigo-500 text-indigo-500" /> Sanos y
              Salvos
            </div>
            <p className="text-sm leading-relaxed text-slate-400">
              Plataforma dedicada a la protección y reencuentro de mascotas
              perdidas. Construyendo una comunidad más segura para nuestros
              mejores amigos.
            </p>
          </div>

          <div>
            <h4 className="text-white font-bold mb-6">Plataforma</h4>
            <ul className="space-y-4 text-sm">
              <li>
                <Link
                  href="/explorar"
                  className="hover:text-white transition-colors"
                >
                  Explorar mascotas
                </Link>
              </li>
              <li>
                <Link
                  href="/reportar"
                  className="hover:text-white transition-colors"
                >
                  Reportar caso
                </Link>
              </li>
              <li>
                <Link
                  href="/blog"
                  className="hover:text-white transition-colors"
                >
                  Consejos de seguridad
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-bold mb-6">Ayuda</h4>
            <ul className="space-y-4 text-sm">
              <li>
                <Link
                  href="/faq"
                  className="hover:text-white transition-colors"
                >
                  Preguntas frecuentes
                </Link>
              </li>
              <li>
                <Link
                  href="/contacto"
                  className="hover:text-white transition-colors"
                >
                  Contacto
                </Link>
              </li>
              <li>
                <Link
                  href="/privacidad"
                  className="hover:text-white transition-colors"
                >
                  Privacidad
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h4 className="text-white font-bold mb-6">Comunidad</h4>
            <div className="flex gap-4 mb-6">
              <a
                href="#"
                className="bg-slate-800 p-3 rounded-xl hover:bg-indigo-600 hover:text-white transition-all"
              >
                <Globe size={20} />
              </a>
              <a
                href="#"
                className="bg-slate-800 p-3 rounded-xl hover:bg-indigo-600 hover:text-white transition-all"
              >
                <MessageCircle size={20} />
              </a>
              <a
                href="#"
                className="bg-slate-800 p-3 rounded-xl hover:bg-indigo-600 hover:text-white transition-all"
              >
                <Share2 size={20} />
              </a>
            </div>
            <div className="flex items-center gap-2 text-sm text-slate-400">
              <Mail size={16} />
              hola@sanosysalvos.cl
            </div>
          </div>
        </div>

        <div className="pt-8 border-t border-slate-800 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-xs text-slate-500">
            © {new Date().getFullYear()} Sanos y Salvos. Todos los derechos
            reservados.
          </p>
          <div className="flex items-center gap-2 text-xs text-emerald-500 bg-emerald-500/10 px-3 py-1 rounded-full">
            <ShieldCheck size={14} />
            Servidor Seguro
          </div>
        </div>
      </div>
    </footer>
  );
}
