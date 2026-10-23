package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.shared.docs.usuario.PacienteControllerDocs;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PacienteController implements PacienteControllerDocs {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @Override
    public void cadastrarPaciente(CadastroPacienteDto cadastroPacienteDto) {
        pacienteService.cadastrarPaciente(cadastroPacienteDto);
    }
}
