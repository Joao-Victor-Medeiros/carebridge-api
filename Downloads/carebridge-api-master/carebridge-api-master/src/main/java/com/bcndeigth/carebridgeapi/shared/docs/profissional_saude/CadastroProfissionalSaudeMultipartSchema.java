package com.bcndeigth.carebridgeapi.shared.docs.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(
        name = "CadastroProfissionalSaudeMultipartSchema",
        description = "Formulário multipart para cadastro de profissional de saúde. A parte dados deve ser enviada como application/json."
)
public record CadastroProfissionalSaudeMultipartSchema(
        @Schema(description = "Parte JSON obrigatória do formulário.", implementation = CadastroProfissionalSaudeDto.class)
        CadastroProfissionalSaudeDto dados,

        @ArraySchema(
                arraySchema = @Schema(
                        description = "Até dois documentos opcionais. Cada arquivo pode ter no máximo 10 MB; a requisição completa pode ter no máximo 22 MB por padrão."
                ),
                maxItems = 2,
                schema = @Schema(type = "string", format = "binary")
        )
        List<MultipartFile> documentos
) {
}