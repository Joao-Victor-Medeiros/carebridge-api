package com.bcndeigth.carebridgeapi.shared.docs.oportunidade;

import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeResponse;
import com.bcndeigth.carebridgeapi.users.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.UUID;

@Tag(name = "Oportunidades", description = "Cadastro e consulta de oportunidades de atendimento.")
@RequestMapping("/oportunidades")
public interface OportunidadeControllerDocs {

    @PostMapping
    @Operation(summary = "Criar oportunidade",
            description = "Cria uma oportunidade para o profissional de saúde autenticado e validado institucionalmente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Oportunidade criada com sucesso.",
                    content = @Content(schema = @Schema(implementation = OportunidadeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Título ausente ou dados inválidos."),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado."),
            @ApiResponse(responseCode = "403", description = "Usuário não é profissional validado institucionalmente.")
    })
    ResponseEntity<OportunidadeResponse> criar(@RequestBody OportunidadeRequest request,
                                               @AuthenticationPrincipal Usuario usuario);

    @GetMapping
    @Operation(summary = "Listar oportunidades ativas")
    List<OportunidadeResponse> listar();

    @GetMapping("/{id}")
    @Operation(summary = "Consultar oportunidade por ID")
    OportunidadeResponse buscar(@PathVariable UUID id);

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar oportunidade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oportunidade atualizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuário não autorizado."),
            @ApiResponse(responseCode = "404", description = "Oportunidade não encontrada.")
    })
    OportunidadeResponse atualizar(@PathVariable UUID id, @RequestBody OportunidadeRequest request,
                                   @AuthenticationPrincipal Usuario usuario);

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir oportunidade")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Oportunidade excluída com sucesso."),
            @ApiResponse(responseCode = "403", description = "Usuário não autorizado."),
            @ApiResponse(responseCode = "409", description = "Oportunidade possui dados relacionados.")
    })
    ResponseEntity<Void> excluir(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuario);
}