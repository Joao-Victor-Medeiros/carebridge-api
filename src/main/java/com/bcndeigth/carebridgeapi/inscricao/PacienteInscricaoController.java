package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.inscricao.dto.InscricaoResponse;
import com.bcndeigth.carebridgeapi.inscricao.dto.MensagemResponse;
import com.bcndeigth.carebridgeapi.inscricao.dto.NovaInscricaoRequest;
import com.bcndeigth.carebridgeapi.inscricao.dto.RespostaPedidoRequest;
import com.bcndeigth.carebridgeapi.inscricao.dto.TextoRequest;
import com.bcndeigth.carebridgeapi.users.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Inscrições do paciente", description = "Solicitações de vaga feitas pelo paciente logado.")
@RestController
@RequestMapping("/paciente/inscricoes")
public class PacienteInscricaoController {

    private final PacienteInscricaoService service;

    public PacienteInscricaoController(PacienteInscricaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Minhas solicitações", description = "Mais recentes primeiro; mensagens vem nulo na lista.")
    public List<InscricaoResponse> listar(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return service.listar(usuario);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe da solicitação, com mensagens")
    public InscricaoResponse detalhar(@PathVariable UUID id,
                                      @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return service.detalhar(id, usuario);
    }

    @PostMapping
    @Operation(summary = "Solicitar vaga", description = "Valida a triagem, os documentos exigidos e o consentimento.")
    public ResponseEntity<InscricaoResponse> inscrever(@RequestBody NovaInscricaoRequest request,
                                                       @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.inscrever(request, usuario));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar solicitação ainda não decidida", description = "Apaga respostas e documentos.")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id,
                                         @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        service.cancelar(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resposta")
    @Operation(summary = "Responder pedido de informação", description = "A solicitação volta para EM_ANALISE.")
    public InscricaoResponse responder(@PathVariable UUID id, @RequestBody RespostaPedidoRequest request,
                                       @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return service.responderPedido(id, request, usuario);
    }

    @GetMapping("/{id}/mensagens")
    @Operation(summary = "Mensagens da solicitação", description = "Marca as do profissional como lidas.")
    public List<MensagemResponse> mensagens(@PathVariable UUID id,
                                            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return service.mensagens(id, usuario);
    }

    @PostMapping("/{id}/mensagens")
    @Operation(summary = "Enviar mensagem", description = "Só depois da aprovação.")
    public ResponseEntity<MensagemResponse> enviar(@PathVariable UUID id, @RequestBody TextoRequest request,
                                                   @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.enviarMensagem(id, request == null ? null : request.texto(), usuario));
    }

    @DeleteMapping("/{id}/documentos")
    @Operation(summary = "Apagar os documentos enviados nesta solicitação")
    public ResponseEntity<Void> apagarDocumentos(@PathVariable UUID id,
                                                 @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        service.apagarDocumentos(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
