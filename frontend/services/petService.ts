const API_BASE_URL = "http://localhost:8082/api/pets";
export interface PetRequestDTO {
  userUid: string;
  nombre: string;
  especie: "PERRO" | "GATO" | "OTRO";
  color: string;
  edad: number;
  descripcion: string;
  fechaPerdida: string;
  estado: "PERDIDO" | "AVISTADO" | "RECUPERADO" | "RETIRADO";
  latitud: number;
  longitud: number;
  direccionTexto?: string;
  foto?: string;
}
export interface PetResponseDTO extends PetRequestDTO {
  id: string;
}

export const petService = {
  getAllPets: async (): Promise<PetResponseDTO[]> => {
    const response = await fetch("http://localhost:8082/api/pets");
    if (!response.ok) throw new Error("Error al obtener mascotas");
    return response.json();
  },

  // 👇 AGREGA ESTA NUEVA FUNCIÓN AQUÍ 👇
  getPetById: async (id: string): Promise<PetResponseDTO> => {
    const response = await fetch(`http://localhost:8082/api/pets/${id}`);
    if (!response.ok)
      throw new Error("Error al obtener el detalle de la mascota");
    return response.json();
  },

  createPet: async (petData: any): Promise<PetResponseDTO> => {
    const response = await fetch("http://localhost:8082/api/pets", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(petData),
    });

    if (!response.ok) {
      throw new Error("Error al crear el reporte de la mascota");
    }

    return response.json();
  },

  createPetReport: async (petData: PetRequestDTO): Promise<PetResponseDTO> => {
    const response = await fetch(API_BASE_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(petData),
    });
    if (!response.ok) throw new Error("Error al crear el reporte de mascota");
    return response.json();
  },

  getPetsByUser: async (userUid: string): Promise<PetResponseDTO[]> => {
    const response = await fetch(`${API_BASE_URL}/user/${userUid}`, {
      cache: "no-store",
    });
    if (!response.ok)
      throw new Error("Error al obtener las mascotas del usuario");
    return response.json();
  },

  updatePet: async (
    id: string,
    petData: PetRequestDTO,
  ): Promise<PetResponseDTO> => {
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(petData),
    });
    if (!response.ok) throw new Error("Error al actualizar la mascota");
    return response.json();
  },

  deletePet: async (id: string): Promise<void> => {
    const response = await fetch(`${API_BASE_URL}/${id}`, { method: "DELETE" });
    if (!response.ok) throw new Error("Error al eliminar el reporte");
  },
};
