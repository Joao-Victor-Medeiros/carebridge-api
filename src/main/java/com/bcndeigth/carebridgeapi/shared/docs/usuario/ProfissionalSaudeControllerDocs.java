package com.bcndeigth.carebridgeapi.shared.docs.usuario;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.shared.docs.profissional_saude.CadastroProfissionalSaudeMultipartSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Usuários", description = "Operações de cadastro de usuários, pacientes e profissionais de saúde.")
@RequestMapping("/profissional-saude")
public interface ProfissionalSaudeControllerDocs {

    @PostMapping(value = "/cadastro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Cadastrar usuário profissional de saúde",
            description = "Cria uma conta de usuário com perfil de profissional de saúde. Envie os dados cadastrais na parte obrigatória dados como application/json e, opcionalmente, até dois arquivos na parte documentos. "
                    + "Cada arquivo pode ter até 10 MB e a requisição completa até 22 MB por padrão.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CadastroProfissionalSaudeMultipartSchema.class),
                            encoding = @Encoding(name = "dados", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário profissional de saúde cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, parte dados ausente, mais de dois documentos ou arquivo vazio/inválido."),
            @ApiResponse(responseCode = "413", description = "Arquivo acima de 10 MB ou requisição multipart acima de 22 MB."),
            @ApiResponse(responseCode = "415", description = "A parte dados não foi enviada com Content-Type application/json.")
    })
    ResponseEntity<Void> cadastrarProfissionalSaude(
            @Validated @RequestPart("dados") CadastroProfissionalSaudeDto cadastroProfissionalSaudeDto,
            @RequestPart(value = "documentos", required = false) List<MultipartFile> documentos
    );
}