package com.bcndeigth.carebridgeapi.profissional_saude;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, UUID> {
    Optional<ProfissionalSaude> findByUsuarioId(UUID usuarioId);

    List<ProfissionalSaude> findBySituacaoRegistroOrderByNomeAsc(SituacaoRegistro situacaoRegistro);
}