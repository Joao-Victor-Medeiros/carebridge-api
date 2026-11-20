package com.bcndeigth.carebridgeapi.profissional_saude.dto;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NonNull;

@Schema(description = "Dados cadastrais de um profissional de saúde.")
public record CadastroProfissionalSaudeDto(
        @NonNull
        @Schema(description = "Dados da conta do profissional.")
        SignUpUserDto usuarioDto,

        @Schema(description = "Nome completo do profissional de saúde.", example = "Dra. Ana Souza")
        String nome,

        @Schema(description = "Conselho profissional.", example = "CRM")
        String conselho,

        @Schema(description = "UF do conselho profissional.", example = "SP")
        String uf,

        @Schema(description = "Número do registro profissional.", example = "123456")
        String numeroRegistro,

        @Schema(description = "Especialidade principal.", example = "Clínica geral")
        String especialidade,

        @Schema(description = "Instituição, clínica ou local de atendimento.", example = "Clínica Saúde")
        String ondeAtende,

        @Schema(description = "Biografia ou descrição profissional.", example = "Médica clínica geral com experiência em atenção primária.")
        String biografiaOuDescricao
) {}
