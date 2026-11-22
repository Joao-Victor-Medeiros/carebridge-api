package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.users.dto.ConfirmacaoSenhaRequest;
import com.bcndeigth.carebridgeapi.users.dto.DataExportResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class MinhaContaController {
    private final MinhaContaService minhaContaService;

    public MinhaContaController(MinhaContaService minhaContaService) {
        this.minhaContaService = minhaContaService;
    }

    @PostMapping("/data-export")
    public DataExportResponse exportar(@AuthenticationPrincipal Usuario usuario) {
        return minhaContaService.exportar(usuario);
    }

    @DeleteMapping("/documents")
    public ResponseEntity<Void> removerDocumentos(@AuthenticationPrincipal Usuario usuario) {
        minhaContaService.removerDocumentosPaciente(usuario);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> anonimizar(
            @AuthenticationPrincipal Usuario usuario,
            @RequestBody ConfirmacaoSenhaRequest request
    ) {
        minhaContaService.anonimizar(usuario, request == null ? null : request.password());
        return ResponseEntity.noContent().build();
    }
}