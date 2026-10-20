package com.bcndeigth.carebridgeapi.profissional_saude.dto;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

import java.util.List;

@Schema(description = "Dados cadastrais de um profissional de saúde.")
public record CadastroProfissionalSaudeDto(
        @NonNull
        @Schema(description = "Dados da conta do profissional.")
        SignUpUserDto usuarioDto,

        @Schema(description = "Referências textuais de documentos de registro profissional.", example = "[\"CRM-SP 123456\"]")
        List<String> documentosRegistro,

        @Schema(description = "Biografia ou descrição profissional.", example = "Médica clínica geral com experiência em atenção primária.")
        String biografiaOuDescricao
) {}
