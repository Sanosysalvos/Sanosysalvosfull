import { useState } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import { LeafletMouseEvent, LatLngTuple } from 'leaflet';
import 'leaflet/dist/leaflet.css';

// 1. Definimos la interfaz para las Props del componente
interface MapPickerProps {
  onLocationSelect: (lat: number, lng: number) => void;
}

export default function MapPicker({ onLocationSelect }: MapPickerProps) {
  // 2. Tipamos el estado como una tupla de coordenadas [latitud, longitud]
  const [position, setPosition] = useState<LatLngTuple>([-33.0245, -71.5518]); 

  // Sub-componente interno para capturar el click del usuario en el mapa
  function LocationMarker() {
    useMapEvents({
      // 3. Tipamos el evento de clic con LeafletMouseEvent
      click(e: LeafletMouseEvent) {
        const { lat, lng } = e.latlng;
        setPosition([lat, lng]);
        onLocationSelect(lat, lng); // Envía los datos tipados numéricamente al formulario
      },
    });

    return position === null ? null : <Marker position={position} />;
  }

  return (
    <div style={{ height: "300px", width: "100%" }}>
      {/* Pasamos position que cumple estrictamente con el tipo LatLngTuple */}
      <MapContainer center={position} zoom={13} style={{ height: "100%", width: "100%" }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <LocationMarker />
      </MapContainer>
    </div>
  );
}