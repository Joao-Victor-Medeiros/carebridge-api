package com.bcndeigth.carebridgeapi.paciente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {
    Optional<Paciente> findByUsuarioId(UUID usuarioId);
}