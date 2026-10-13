package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/paciente")
public class PacienteController {

    @PostMapping("/cadastro")
    public String cadastrarPaciente(@RequestBody CadastroPacienteDto cadastroPacienteDto) {
        // Lógica para cadastrar o paciente
        return "Paciente cadastrado com sucesso!";
    }
}
