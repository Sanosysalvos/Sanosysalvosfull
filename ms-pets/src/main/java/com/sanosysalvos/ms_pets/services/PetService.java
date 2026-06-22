package com.sanosysalvos.ms_pets.services;

import com.sanosysalvos.ms_pets.clients.UserClient;
import com.sanosysalvos.ms_pets.dtos.PetHeatmapDTO;
import com.sanosysalvos.ms_pets.dtos.PetRequestDTO;
import com.sanosysalvos.ms_pets.dtos.PetResponseDTO;
import com.sanosysalvos.ms_pets.mappers.PetMapper;
import com.sanosysalvos.ms_pets.models.Pet;
import com.sanosysalvos.ms_pets.repositories.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;
    private final UserClient userClient;

    @Transactional
    public PetResponseDTO crearReporte(PetRequestDTO dto) {
        // Resolver el Firebase UID a UUID llamando a ms-users
        UUID userUuid = userClient.getUuidByFirebaseUid(dto.getUserUid());
        
        // Crear la entidad Pet a partir del DTO
        Pet pet = petMapper.toEntity(dto);
        
        // Asignar el UUID resuelto
        pet.setUserUid(userUuid);
        
        // Guardar en base de datos
        Pet guardada = petRepository.save(pet);
        return petMapper.toResponseDTO(guardada);
    }

  @Transactional(readOnly = true)
  public List<PetResponseDTO> obtenerTodas(boolean recientes) {
    List<Pet> mascotas;

    if (recientes) {
        // Trae la página 0, con un límite de 9 registros, ordenados por ID de forma descendente (las más nuevas primero)
        Pageable topNueve = PageRequest.of(0, 9, Sort.by("id").descending());
        mascotas = petRepository.findAll(topNueve).getContent();
    } else {
        // Comportamiento original para la pantalla de explorar con filtros
        mascotas = petRepository.findAll();
    }

    // Tu mismo mapeo de siempre, impecable
    return mascotas.stream()
            .map(petMapper::toResponseDTO)
            .collect(Collectors.toList());
}

    @Transactional(readOnly = true)
    public PetResponseDTO obtenerPorId(UUID id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada con el ID: " + id));
        return petMapper.toResponseDTO(pet);
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> obtenerPorUsuario(String firebaseUid) {
    UUID userId = userClient.getUuidByFirebaseUid(firebaseUid);
    return petRepository.findByUserUid(userId).stream()
                .map(petMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PetResponseDTO actualizarMascota(UUID id, PetRequestDTO dto) {
        Pet petExistente = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede actualizar. Mascota no encontrada."));

        petMapper.updateEntityFromDto(dto, petExistente);
        Pet actualizada = petRepository.save(petExistente);
        return petMapper.toResponseDTO(actualizada);
    }

    @Transactional
    public void eliminarReporte(UUID id) {
        if (!petRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar. El reporte de mascota no existe.");
        }
        petRepository.deleteById(id);
    }
     // Método para heatmap
    public List<PetHeatmapDTO> getLostPetsForHeatmap(Integer horas, String especie) {
        // Si hay filtro de horas, calcular fecha límite
        LocalDate fechaLimite = null;
        if (horas != null && horas > 0) {
            fechaLimite = LocalDate.now().minusDays(horas / 24); // Convertir horas a días
        }
        
        // Filtrar especie solo si no es "todos"
        String especieFilter = null;
        if (especie != null && !especie.isEmpty() && !"todos".equalsIgnoreCase(especie)) {
            especieFilter = especie;
        }
        
        List<Pet> pets = petRepository.findLostPetsForHeatmap(fechaLimite, especieFilter);
        
        return pets.stream()
            .map(PetHeatmapDTO::new)
            .collect(Collectors.toList());
    }

    // 🚀 MÉTODO CONECTADO AL MAPPER PARA FLUJO PREDICTIVO (MS-MATCHING)
    @Transactional(readOnly = true)
    public List<PetResponseDTO> obtenerPorEstado(String estado) {
        // Ejecutamos la query pasando 'null' en la especie para que traiga todos los candidatos genéricos
        List<Pet> mascotas = petRepository.findByEstadoAndLatitudIsNotNullAndLongitudIsNotNull(null);
        
        // Usamos tu mapper impecable para transformar todo a DTOs de forma automática
        return mascotas.stream()
                .map(petMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
}