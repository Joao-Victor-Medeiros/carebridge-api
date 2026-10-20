package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/profissional-saude")
public class ProfissionalSaudeController {

    @Autowired
    private ProfissionalSaudeService profissionalSaudeService;

    @PostMapping(
            value = "/cadastro",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> cadastrarProfissionalSaude(
            @Validated @RequestPart("dados") CadastroProfissionalSaudeDto cadastroProfissionalSaudeDto,
            @RequestPart(value = "documentos", required = false) List<MultipartFile> documentos
    ) {
        profissionalSaudeService.cadastrarProfissionalSaude(cadastroProfissionalSaudeDto, documentos);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}