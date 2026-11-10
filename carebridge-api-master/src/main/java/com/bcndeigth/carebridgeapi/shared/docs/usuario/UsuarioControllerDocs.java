package com.bcndeigth.carebridgeapi.shared.docs.usuario;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Usuários", description = "Operações de cadastro de usuários, pacientes e profissionais de saúde.")
@RequestMapping("/user")
public interface UsuarioControllerDocs {

    @PostMapping("/register")
    @Operation(
            summary = "Cadastrar usuário genérico",
            description = "Cria uma conta de usuário a partir de dados JSON, utilizando o tipo de perfil informado no campo tipoPerfil."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados de cadastro inválidos.",
                    content = @Content(schema = @Schema(implementation = Object.class)))
    })
    void signUp(@RequestBody SignUpUserDto dto);
}