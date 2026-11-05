package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeResponse;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/oportunidades")
public class OportunidadeController {
    private final OportunidadeService oportunidadeService;

    public OportunidadeController(OportunidadeService oportunidadeService) {
        this.oportunidadeService = oportunidadeService;
    }

    @PostMapping
    @PreAuthorize("@oportunidadeAuthorization.podeGerenciar(authentication)")
    public ResponseEntity<OportunidadeResponse> criar(@RequestBody OportunidadeRequest request,
                                                       @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oportunidadeService.criar(request, usuario));
    }

    @GetMapping
    public List<OportunidadeResponse> listar() {
        return oportunidadeService.listar();
    }

    @GetMapping("/{id}")
    public OportunidadeResponse buscar(@PathVariable UUID id) {
        return oportunidadeService.buscar(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@oportunidadeAuthorization.podeGerenciar(authentication)")
    public OportunidadeResponse atualizar(@PathVariable UUID id, @RequestBody OportunidadeRequest request,
                                          @AuthenticationPrincipal Usuario usuario) {
        return oportunidadeService.atualizar(id, request, usuario);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@oportunidadeAuthorization.podeGerenciar(authentication)")
    public ResponseEntity<Void> excluir(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuario) {
        oportunidadeService.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }
}