package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import com.bcndeigth.carebridgeapi.shared.docs.usuario.ProfissionalSaudeControllerDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ProfissionalSaudeController implements ProfissionalSaudeControllerDocs {

    @Autowired
    private ProfissionalSaudeService profissionalSaudeService;

    @Override
    public ResponseEntity<Void> cadastrarProfissionalSaude(
            CadastroProfissionalSaudeDto cadastroProfissionalSaudeDto,
            List<MultipartFile> documentos
    ) {
        profissionalSaudeService.cadastrarProfissionalSaude(cadastroProfissionalSaudeDto, documentos);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}