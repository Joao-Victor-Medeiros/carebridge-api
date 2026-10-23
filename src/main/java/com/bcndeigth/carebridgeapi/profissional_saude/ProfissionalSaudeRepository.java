package com.bcndeigth.carebridgeapi.profissional_saude;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, UUID> {
}