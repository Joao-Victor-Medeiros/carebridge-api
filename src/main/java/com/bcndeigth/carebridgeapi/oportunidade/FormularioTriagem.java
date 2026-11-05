package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FormularioTriagem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private ProfissionalSaude ofertante;

    // Adicione outros atributos relevantes aqui
}
