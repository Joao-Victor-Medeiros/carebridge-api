package com.bcndeigth.carebridgeapi.profissional_saude.dto;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

@Schema(description = "Dados cadastrais de um profissional de saúde.")
public record CadastroProfissionalSaudeDto(
        @NonNull
        @Schema(description = "Dados da conta do profissional.")
        SignUpUserDto usuarioDto,

        @Schema(description = "Nome completo do profissional de saúde.", example = "Maria Santos")
        String nome,

        @Schema(description = "Conselho profissional.", example = "CRM")
        String conselho,

        @Schema(description = "UF do conselho profissional.", example = "DF")
        String uf,

        @Schema(description = "Número do registro profissional.", example = "123456")
        String numeroRegistro,

        @Schema(description = "Especialidade principal.", example = "Cardiologia")
        String especialidade,

        @Schema(description = "Instituição, clínica ou local de atendimento.", example = "Brasília - DF")
        String ondeAtende,

        @Schema(description = "Biografia ou descrição profissional.", example = "Médica cardiologista com experiência clínica.")
        String biografiaOuDescricao,

        @Schema(description = "Indica se os termos de uso foram aceitos.", example = "true")
        Boolean acceptedTerms
) {}