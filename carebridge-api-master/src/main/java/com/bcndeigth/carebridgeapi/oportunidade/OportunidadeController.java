package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeResponse;
import com.bcndeigth.carebridgeapi.users.Usuario;
import com.bcndeigth.carebridgeapi.shared.docs.oportunidade.OportunidadeControllerDocs;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class OportunidadeController implements OportunidadeControllerDocs {
    private final OportunidadeService oportunidadeService;

    public OportunidadeController(OportunidadeService oportunidadeService) {
        this.oportunidadeService = oportunidadeService;
    }

    @PreAuthorize("@oportunidadeAuthorization.podeGerenciar(authentication)")
    @Override
    public ResponseEntity<OportunidadeResponse> criar(@RequestBody OportunidadeRequest request,
                                                      @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(201).body(oportunidadeService.criar(request, usuario));
    }

    @Override
    public List<OportunidadeResponse> listar() {
        return oportunidadeService.listar();
    }

    @Override
    public OportunidadeResponse buscar(@PathVariable UUID id) {
        return oportunidadeService.buscar(id);
    }

    @PreAuthorize("@oportunidadeAuthorization.podeGerenciar(authentication)")
    @Override
    public OportunidadeResponse atualizar(@PathVariable UUID id, @RequestBody OportunidadeRequest request,
                                          @AuthenticationPrincipal Usuario usuario) {
        return oportunidadeService.atualizar(id, request, usuario);
    }

    @PreAuthorize("@oportunidadeAuthorization.podeGerenciar(authentication)")
    @Override
    public ResponseEntity<Void> excluir(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuario) {
        oportunidadeService.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }
}