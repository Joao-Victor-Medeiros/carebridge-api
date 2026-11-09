package com.bcndeigth.carebridgeapi.profissional_saude;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, UUID> {
    Optional<ProfissionalSaude> findByUsuarioId(UUID usuarioId);
    List<ProfissionalSaude> findByValidadoInstitucionalmenteFalse();
}