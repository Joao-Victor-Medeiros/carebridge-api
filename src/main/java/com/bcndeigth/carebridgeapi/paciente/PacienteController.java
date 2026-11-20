package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.paciente.dto.CadastroPacienteDto;
import com.bcndeigth.carebridgeapi.shared.docs.usuario.PacienteControllerDocs;
import com.bcndeigth.carebridgeapi.users.dto.CadastroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/paciente")
public class PacienteController implements PacienteControllerDocs {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @Override
    public ResponseEntity<CadastroResponse> cadastrarPaciente(CadastroPacienteDto cadastroPacienteDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pacienteService.cadastrarPaciente(cadastroPacienteDto));
    }
}
