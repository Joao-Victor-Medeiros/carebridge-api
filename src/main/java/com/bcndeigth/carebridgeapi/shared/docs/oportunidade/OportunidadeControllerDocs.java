package com.bcndeigth.carebridgeapi.shared.docs.oportunidade;

import com.bcndeigth.carebridgeapi.oportunidade.Modalidade;
import com.bcndeigth.carebridgeapi.oportunidade.ModalidadeAcesso;
import com.bcndeigth.carebridgeapi.oportunidade.PublicoAlvo;
import com.bcndeigth.carebridgeapi.oportunidade.TipoServico;
import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeResponse;
import com.bcndeigth.carebridgeapi.shared.PaginaResponse;
import com.bcndeigth.carebridgeapi.users.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Oportunidades", description = "Busca pública e gestão das oportunidades pelo profissional.")
public interface OportunidadeControllerDocs {

    @GetMapping("/oportunidades")
    @Operation(summary = "Buscar oportunidades ativas",
            description = "Pública. Listas aceitam valores separados por vírgula. distanciaMaxKm é aceito, "
                    + "mas ainda não filtra: a distância só existe com geolocalização.")
    PaginaResponse<OportunidadeResponse> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TipoServico tipoServico,
            @RequestParam(required = false) List<String> especialidade,
            @RequestParam(required = false) Modalidade modalidade,
            @RequestParam(required = false) Integer distanciaMaxKm,
            @RequestParam(required = false) List<ModalidadeAcesso> modalidadeAcesso,
            @RequestParam(required = false) List<PublicoAlvo> publicoAlvo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size);

    @GetMapping("/oportunidades/{id}")
    @Operation(summary = "Detalhar oportunidade", description = "Pública. Rascunho só aparece para o dono.")
    @ApiResponse(responseCode = "404", description = "Não existe, ou é rascunho de outro profissional.")
    OportunidadeResponse detalhar(@PathVariable UUID id,
                                  @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario);

    @GetMapping("/profissional-saude/oportunidades")
    @Operation(summary = "Minhas oportunidades", description = "Todas as do profissional logado, com totalInscricoes.")
    List<OportunidadeResponse> minhas(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario);

    @PostMapping("/oportunidades")
    @Operation(summary = "Criar oportunidade",
            description = "status RASCUNHO salva com campos vazios; qualquer outro valor publica e valida tudo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criada."),
            @ApiResponse(responseCode = "400", description = "Falta campo para publicar ou triagem inválida."),
            @ApiResponse(responseCode = "403", description = "Profissional ainda em verificação tentando publicar.")
    })
    ResponseEntity<OportunidadeResponse> criar(@RequestBody OportunidadeRequest request,
                                               @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario);

    @PutMapping("/oportunidades/{id}")
    @Operation(summary = "Editar oportunidade", description = "Só o dono. O que já foi publicado não volta a rascunho.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Não existe ou não é do profissional."),
            @ApiResponse(responseCode = "409", description = "Encerrada, ou vagas abaixo das já preenchidas.")
    })
    OportunidadeResponse atualizar(@PathVariable UUID id, @RequestBody OportunidadeRequest request,
                                   @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario);

    @PostMapping("/oportunidades/{id}/encerrar")
    @Operation(summary = "Encerrar oportunidade", description = "Só o dono e só ATIVA.")
    @ApiResponse(responseCode = "409", description = "Não está ativa.")
    ResponseEntity<Void> encerrar(@PathVariable UUID id,
                                  @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario);

    @DeleteMapping("/oportunidades/{id}")
    @Operation(summary = "Excluir rascunho", description = "Só o dono e só RASCUNHO.")
    @ApiResponse(responseCode = "409", description = "Não é rascunho.")
    ResponseEntity<Void> excluir(@PathVariable UUID id,
                                 @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario);
}
