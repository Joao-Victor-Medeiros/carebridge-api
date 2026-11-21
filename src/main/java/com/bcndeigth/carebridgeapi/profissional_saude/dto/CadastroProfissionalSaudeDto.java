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

        @Schema(description = "Nome completo do profissional de saúde.", example = "Maria Santos")
        String nome,

        @Schema(description = "Conselho profissional.", example = "CRM")
        String conselho,

        @Schema(description = "UF do registro profissional.", example = "DF")
        String uf,

        @Schema(description = "Número do registro profissional.", example = "123456")
        String numeroRegistro,

        @Schema(description = "Especialidade do profissional.", example = "Cardiologia")
        String especialidade,

        @Schema(description = "Local onde o profissional atende.", example = "Brasília - DF")
        String ondeAtende,

        @Schema(description = "Referências textuais de documentos de registro profissional.", example = "[\"CRM-DF 123456\"]")
        List<String> documentosRegistro,

        @Schema(description = "Biografia ou descrição profissional.", example = "Médica cardiologista com experiência clínica.")
        String biografiaOuDescricao,

        @Schema(description = "Indica se os termos de uso foram aceitos.", example = "true")
        Boolean acceptedTerms
) {}