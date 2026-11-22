package com.bcndeigth.carebridgeapi.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CodigoVerificacaoEmailRepository extends JpaRepository<CodigoVerificacaoEmail, UUID> {
    Optional<CodigoVerificacaoEmail> findFirstByUsuarioIdAndFinalidadeOrderByCriadoEmDesc(
            UUID usuarioId,
            FinalidadeCodigo finalidade
    );

    void deleteByUsuarioId(UUID usuarioId);
}