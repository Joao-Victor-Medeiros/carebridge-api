package com.bcndeigth.carebridgeapi.auth.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(
        name = "CadastroUsuarioMultipart",
        description = "Formulário multipart polimórfico para cadastro de paciente ou profissional de saúde."
)
public record CadastroUsuarioMultipartSchema(
        @Schema(
                description = "JSON polimórfico. O campo tipoPerfil deve ser PACIENTE ou PROFISSIONAL_SAUDE.",
                implementation = CadastroUsuarioRequest.class
        )
        CadastroUsuarioRequest dados,

        @ArraySchema(
                arraySchema = @Schema(
                        description = "Até dois documentos PDF, JPG ou PNG, aceitos somente para profissionais."
                ),
                schema = @Schema(type = "string", format = "binary"),
                maxItems = 2
        )
        List<MultipartFile> documentos
) {
}