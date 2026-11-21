package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeRequest;
import com.bcndeigth.carebridgeapi.oportunidade.dto.OportunidadeResponse;
import com.bcndeigth.carebridgeapi.shared.PaginaResponse;
import com.bcndeigth.carebridgeapi.shared.docs.oportunidade.OportunidadeControllerDocs;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class OportunidadeController implements OportunidadeControllerDocs {

    private final OportunidadeService service;

    public OportunidadeController(OportunidadeService service) {
        this.service = service;
    }

    @Override
    public PaginaResponse<OportunidadeResponse> buscar(String q, TipoServico tipoServico, List<String> especialidade,
                                                       Modalidade modalidade, Integer distanciaMaxKm,
                                                       List<ModalidadeAcesso> modalidadeAcesso,
                                                       List<PublicoAlvo> publicoAlvo, int page, int size) {
        var busca = new BuscaOportunidades(q, tipoServico, especialidade, modalidade, modalidadeAcesso, publicoAlvo);
        return service.buscar(busca, page, size);
    }

    @Override
    public OportunidadeResponse detalhar(UUID id, Usuario usuario) {
        return service.detalhar(id, usuario);
    }

    @Override
    public List<OportunidadeResponse> minhas(Usuario usuario) {
        return service.minhas(usuario);
    }

    @Override
    public ResponseEntity<OportunidadeResponse> criar(OportunidadeRequest request, Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request, usuario));
    }

    @Override
    public OportunidadeResponse atualizar(UUID id, OportunidadeRequest request, Usuario usuario) {
        return service.atualizar(id, request, usuario);
    }

    @Override
    public ResponseEntity<Void> encerrar(UUID id, Usuario usuario) {
        service.encerrar(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> excluir(UUID id, Usuario usuario) {
        service.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
