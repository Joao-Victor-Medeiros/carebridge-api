package com.bcndeigth.carebridgeapi.inscricao;

import com.bcndeigth.carebridgeapi.inscricao.dto.DecisaoRequest;
import com.bcndeigth.carebridgeapi.inscricao.dto.InscricaoResponse;
import com.bcndeigth.carebridgeapi.inscricao.dto.MensagemResponse;
import com.bcndeigth.carebridgeapi.inscricao.dto.TextoRequest;
import com.bcndeigth.carebridgeapi.users.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Inscrições recebidas", description = "Solicitações nas oportunidades do profissional logado.")
@RestController
@RequestMapping("/profissional-saude/inscricoes")
public class ProfissionalInscricaoController {

    private final ProfissionalInscricaoService service;

    public ProfissionalInscricaoController(ProfissionalInscricaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Solicitações recebidas", description = "Filtro opcional por status.")
    public List<InscricaoResponse> listar(@RequestParam(required = false) StatusInscricao status,
                                          @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return service.listar(status, usuario);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe da solicitação", description = "Marca as mensagens do paciente como lidas.")
    public InscricaoResponse detalhar(@PathVariable UUID id,
                                      @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return service.detalhar(id, usuario);
    }

    @PostMapping("/{id}/decisao")
    @Operation(summary = "Aprovar ou recusar", description = "Aprovar ocupa uma vaga; recusar exige motivo.")
    public InscricaoResponse decidir(@PathVariable UUID id, @RequestBody DecisaoRequest request,
                                     @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return service.decidir(id, request, usuario);
    }

    @PostMapping("/{id}/pedido-informacao")
    @Operation(summary = "Pedir informação ao paciente", description = "Prazo de 3 dias para responder.")
    public InscricaoResponse pedirInformacao(@PathVariable UUID id, @RequestBody TextoRequest request,
                                             @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return service.pedirInformacao(id, request == null ? null : request.texto(), usuario);
    }

    @PostMapping("/{id}/mensagens")
    @Operation(summary = "Enviar mensagem ao paciente", description = "Só depois da aprovação.")
    public ResponseEntity<MensagemResponse> enviar(@PathVariable UUID id, @RequestBody TextoRequest request,
                                                   @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.enviarMensagem(id, request == null ? null : request.texto(), usuario));
    }
}
