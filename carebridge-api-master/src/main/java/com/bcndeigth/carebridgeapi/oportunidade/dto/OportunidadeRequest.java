package com.bcndeigth.carebridgeapi.oportunidade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para criar ou atualizar uma oportunidade de atendimento.")
public record OportunidadeRequest(
        @Schema(description = "Título da oportunidade.", example = "Atendimento psicológico gratuito")
        String titulo,

        @Schema(description = "Descrição detalhada da oportunidade.",
                example = "Atendimento psicológico individual para adultos, realizado por profissional habilitado.")
        String descricao,

        @Schema(description = "Tipo de serviço oferecido.", example = "Psicologia")
        String tipoServico,

        @Schema(description = "Público que pode utilizar o serviço.", example = "Adultos de baixa renda")
        String publicoAlvo,

        @Schema(description = "Modalidade de atendimento.", example = "ONLINE")
        String modalidade,

        @Schema(description = "Condições e regras para acesso ao serviço.",
                example = "Necessário apresentar documento de identificação e comprovante de renda.")
        String condicoesAcesso,

        @Schema(description = "Define se a oportunidade ficará disponível para os pacientes.", example = "true")
        boolean statusAtiva
) {
}