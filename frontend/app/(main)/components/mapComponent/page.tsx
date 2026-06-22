"use client";

import { useEffect, useRef } from "react";

interface Marker {
  lat: number;
  lng: number;
  type: "perdido" | "avistamiento";
  label?: string;
  foto?: string;
  comentario?: string;
  fecha?: string;
}

interface MapComponentProps {
  centerLat: number;
  centerLng: number;
  markers?: Marker[];
  onMapClick?: (lat: number, lng: number) => void;
  interactive?: boolean;
  height?: string;
}

export default function MapComponent({
  centerLat,
  centerLng,
  markers = [],
  onMapClick,
  interactive = false,
  height = "400px",
}: MapComponentProps) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<any>(null);
  const clickMarkerRef = useRef<any>(null);

  useEffect(() => {
    if (typeof window === "undefined" || !mapRef.current) return;

    const initMap = async () => {
      const L = (await import("leaflet")).default;
      await import("leaflet/dist/leaflet.css");

      delete (L.Icon.Default.prototype as any)._getIconUrl;
      L.Icon.Default.mergeOptions({
        iconRetinaUrl:
          "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
        iconUrl:
          "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
        shadowUrl:
          "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
      });

      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }

      const map = L.map(mapRef.current!).setView([centerLat, centerLng], 14);
      mapInstanceRef.current = map;

      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution:
          '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        maxZoom: 19,
      }).addTo(map);

      // Ícono rojo — lugar de pérdida
      const iconoPerdido = L.divIcon({
        className: "",
        html: `<div style="
          width:36px;height:36px;border-radius:50% 50% 50% 0;
          background:#ef4444;border:3px solid #fff;
          transform:rotate(-45deg);box-shadow:0 2px 8px rgba(0,0,0,0.3);
        "></div>`,
        iconSize: [36, 36],
        iconAnchor: [18, 36],
        popupAnchor: [0, -36],
      });

      // Ícono azul cielo — avistamiento
      const iconoAvistamiento = L.divIcon({
        className: "",
        html: `<div style="
          width:30px;height:30px;border-radius:50% 50% 50% 0;
          background:#0ea5e9;border:3px solid #fff;
          transform:rotate(-45deg);box-shadow:0 2px 8px rgba(0,0,0,0.3);
        "></div>`,
        iconSize: [30, 30],
        iconAnchor: [15, 30],
        popupAnchor: [0, -30],
      });

      // Ícono verde — click del usuario
      const iconoClick = L.divIcon({
        className: "",
        html: `<div style="
          width:30px;height:30px;border-radius:50% 50% 50% 0;
          background:#22c55e;border:3px solid #fff;
          transform:rotate(-45deg);box-shadow:0 2px 8px rgba(0,0,0,0.3);
        "></div>`,
        iconSize: [30, 30],
        iconAnchor: [15, 30],
        popupAnchor: [0, -30],
      });

      markers.forEach((m) => {
        const icono = m.type === "perdido" ? iconoPerdido : iconoAvistamiento;
        const popupContent =
          m.type === "perdido"
            ? `<div style="font-family:sans-serif;min-width:160px;">
                <div style="font-weight:700;color:#ef4444;margin-bottom:4px;">📍 Último lugar visto</div>
                <div style="font-size:13px;color:#374151;">${m.label || "Mascota perdida"}</div>
              </div>`
            : `<div style="font-family:sans-serif;min-width:180px;">
                ${m.foto ? `<img src="${m.foto}" style="width:100%;height:90px;object-fit:cover;border-radius:6px;margin-bottom:6px;" />` : ""}
                <div style="font-weight:700;color:#0ea5e9;margin-bottom:4px;">👁 Avistamiento</div>
                ${m.comentario ? `<div style="font-size:12px;color:#374151;margin-bottom:4px;">"${m.comentario}"</div>` : ""}
                ${m.fecha ? `<div style="font-size:11px;color:#9ca3af;">${new Date(m.fecha).toLocaleDateString("es-CL")}</div>` : ""}
              </div>`;

        L.marker([m.lat, m.lng], { icon: icono })
          .addTo(map)
          .bindPopup(popupContent);
      });

      if (interactive && onMapClick) {
        map.on("click", (e: any) => {
          const { lat, lng } = e.latlng;
          if (clickMarkerRef.current) {
            clickMarkerRef.current.remove();
          }
          clickMarkerRef.current = L.marker([lat, lng], { icon: iconoClick })
            .addTo(map)
            .bindPopup(
              `<div style="font-family:sans-serif;font-size:13px;color:#374151;">
                📍 Avistamiento marcado aquí
              </div>`
            )
            .openPopup();
          onMapClick(lat, lng);
        });
      }
    };

    initMap();

    return () => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, [centerLat, centerLng, markers, interactive, onMapClick]);

  return (
    <div
      ref={mapRef}
      style={{ height, width: "100%", borderRadius: "1.25rem", zIndex: 0 }}
    />
  );
}