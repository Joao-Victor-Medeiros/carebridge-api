package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DataExportResponse(
        UUID id,
        String email,
        TipoPerfil tipoPerfil,
        boolean emailVerificado,
        Instant termosAceitosEm,
        Instant criadoEm,
        Instant atualizadoEm,
        Map<String, Object> perfil,
        List<Map<String, Object>> registrosRelacionados,
        Instant exportadoEm
) {
}