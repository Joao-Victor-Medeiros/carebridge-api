package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.profissional_saude.dto.CadastroProfissionalSaudeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profissional-saude")
public class ProfissionalSaudeController {

    @Autowired
    private ProfissionalSaudeService profissionalSaudeService;

    @PostMapping("/cadastro")
    public void cadastrarProfissionalSaude(@RequestBody CadastroProfissionalSaudeDto cadastroProfissionalSaudeDto) {
        profissionalSaudeService.cadastrarProfissionalSaude(cadastroProfissionalSaudeDto);
    }
}