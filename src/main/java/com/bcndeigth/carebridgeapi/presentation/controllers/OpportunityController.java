package com.bcndeigth.carebridgeapi.presentation.controllers;

import com.bcndeigth.carebridgeapi.application.usecases.ListarOportunidadesUseCase;
import com.bcndeigth.carebridgeapi.domain.entities.Oportunidade;
import com.bcndeigth.carebridgeapi.presentation.dtos.OportunidadePageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/opportunities") // A rota exata exigida pelo contrato do iOS
public class OpportunityController {

    private final ListarOportunidadesUseCase listarOportunidadesUseCase;

    public OpportunityController(ListarOportunidadesUseCase listarOportunidadesUseCase) {
        this.listarOportunidadesUseCase = listarOportunidadesUseCase;
    }

    @GetMapping
    public ResponseEntity<OportunidadePageDTO> listarOportunidades(
            @RequestParam(defaultValue = "1") int page) {
        
        // 1. Pede os dados puros à camada de Aplicação (que fala com a Infraestrutura)
        List<Oportunidade> oportunidades = listarOportunidadesUseCase.executar();
        
        // 2. Prepara a "caixa" (DTO) para enviar para a internet
        OportunidadePageDTO response = new OportunidadePageDTO();
        response.page = page;
        response.totalPages = 1;
        response.totalItems = oportunidades.size();
        response.items = new ArrayList<>();

        // 3. Traduz os dados puros para o formato exato que o ecrã do telemóvel espera
        for (Oportunidade op : oportunidades) {
            OportunidadePageDTO.OportunidadeItemDTO itemDTO = new OportunidadePageDTO.OportunidadeItemDTO();
            itemDTO.id = op.getId();
            itemDTO.title = op.getId().equals("op-001") ? "Psicologia online" : "Ortopedia"; 
            itemDTO.specialty = "psicologia";
            itemDTO.modality = op.getModalidade();
            itemDTO.offerType = op.getTipoOferta();
            itemDTO.vacancies = op.getQuantidadeVagas();
            
            itemDTO.provider = new OportunidadePageDTO.ProviderDTO();
            itemDTO.provider.id = op.getOfertanteId();
            itemDTO.provider.name = op.getId().equals("op-001") ? "Clínica Saúde" : "Boca Feliz";
            itemDTO.provider.kind = "INSTITUICAO";
            itemDTO.provider.verified = true;

            itemDTO.location = new OportunidadePageDTO.LocationDTO();
            itemDTO.location.city = "Brasília";
            itemDTO.location.state = "DF";
            itemDTO.location.address = op.getId().equals("op-001") ? "Remoto" : "QNM 34, Ceilândia";

            response.items.add(itemDTO);
        }
        
        return ResponseEntity.ok(response);
    }
}