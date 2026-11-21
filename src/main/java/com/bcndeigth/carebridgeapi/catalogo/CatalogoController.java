package com.bcndeigth.carebridgeapi.catalogo;

import com.bcndeigth.carebridgeapi.catalogo.CatalogoResponse.Conselho;
import com.bcndeigth.carebridgeapi.catalogo.CatalogoResponse.Especialidade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Catálogo", description = "Valores fixos usados nos formulários e filtros dos apps.")
@RestController
public class CatalogoController {

    static final CatalogoResponse CATALOGO = new CatalogoResponse(
            List.of(
                    new Especialidade("cardiologia", "Cardiologia"),
                    new Especialidade("clinica-geral", "Clínica geral"),
                    new Especialidade("fisioterapia", "Fisioterapia"),
                    new Especialidade("fonoaudiologia", "Fonoaudiologia"),
                    new Especialidade("nutricao", "Nutrição"),
                    new Especialidade("odontologia", "Odontologia"),
                    new Especialidade("ortopedia", "Ortopedia"),
                    new Especialidade("psicologia", "Psicologia"),
                    new Especialidade("terapia-ocupacional", "Terapia ocupacional")
            ),
            List.of(
                    new Conselho("CRM", "Medicina"),
                    new Conselho("COREN", "Enfermagem"),
                    new Conselho("CRO", "Odontologia"),
                    new Conselho("CRP", "Psicologia"),
                    new Conselho("CREFITO", "Fisioterapia e TO"),
                    new Conselho("CRN", "Nutrição"),
                    new Conselho("CRF", "Farmácia"),
                    new Conselho("CREFONO", "Fonoaudiologia"),
                    new Conselho("CRESS", "Serviço social"),
                    new Conselho("CREF", "Educação física"),
                    new Conselho("CRBM", "Biomedicina")
            ),
            List.of("AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MG", "MS", "MT", "PA",
                    "PB", "PE", "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO"),
            List.of(2, 5, 10)
    );

    @GetMapping("/catalogos")
    @Operation(summary = "Catálogo de especialidades, conselhos, UFs e distâncias")
    public CatalogoResponse catalogo() {
        return CATALOGO;
    }
}
