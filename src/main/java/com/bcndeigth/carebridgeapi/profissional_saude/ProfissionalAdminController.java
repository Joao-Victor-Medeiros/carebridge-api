package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.users.dto.MeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user/profissionais")
public class ProfissionalAdminController {

    private final ProfissionalAdminService service;

    public ProfissionalAdminController(ProfissionalAdminService service) {
        this.service = service;
    }

    @GetMapping("/pendentes")
    public List<MeResponse> pendentes() {
        return service.pendentes();
    }

    @PatchMapping("/{id}/aprovar")
    public MeResponse aprovar(@PathVariable UUID id) {
        return service.alterarSituacao(id, SituacaoRegistro.VERIFICADO);
    }

    @PatchMapping("/{id}/recusar")
    public MeResponse recusar(@PathVariable UUID id) {
        return service.alterarSituacao(id, SituacaoRegistro.RECUSADO);
    }
}
