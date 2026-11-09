package com.bcndeigth.carebridgeapi.shared.docs.usuario;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
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
@RequestMapping("/paciente")
public interface PacienteControllerDocs {

    @PostMapping("/cadastro")
    @Operation(
            summary = "Cadastrar usuário paciente",
            description = "Cria uma conta de usuário com perfil de paciente e registra seus dados de nascimento, telefone e endereço."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário paciente cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados de cadastro inválidos.",
                    content = @Content(schema = @Schema(implementation = Object.class)))
    })
    void cadastrarPaciente(@RequestBody CadastroPacienteDto cadastroPacienteDto);
}