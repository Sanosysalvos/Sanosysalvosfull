package com.sanosysalvos.ms_users.repositories; 

import com.sanosysalvos.ms_users.models.User; // Importa la ruta exacta
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByFirebaseUid(String firebaseUid);
    Boolean existsByEmail(String email);
    Boolean existsByRut(String rut);
    Optional<User> findByEmail(String email);
}