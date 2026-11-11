package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de um profissional de saúde aguardando aprovação administrativa.")
public record ProfissionalPendenteResponse(
        UUID profissionalId,
        UUID usuarioId,
        String nome,
        String email,
        List<String> documentosRegistro,
        String biografiaOuDescricao,
        boolean validadoInstitucionalmente
) {
    public static ProfissionalPendenteResponse from(ProfissionalSaude profissional) {
        return new ProfissionalPendenteResponse(
                profissional.getId(),
                profissional.getUsuario().getId(),
                profissional.getNome(),
                profissional.getUsuario().getEmail(),
                profissional.getDocumentosRegistro().stream()
                        .map(documento -> documento.getNomeArquivo())
                        .toList(),
                profissional.getBiografiaOuDescricao(),
                profissional.isValidadoInstitucionalmente()
        );
    }
}