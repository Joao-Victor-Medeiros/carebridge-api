package com.bcndeigth.carebridgeapi.opportunities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;

@Entity
@Table(name = "formulario_triagem")
@Getter
@Setter
@NoArgsConstructor
public class FormularioTriagem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "oportunidade_id",
            nullable = false,
            unique = true
    )
    private Oportunidade oportunidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ofertante_id", nullable = false)
    private ProfissionalSaude ofertante;

    @Column(name = "configuracao_campos", columnDefinition = "text")
    private String configuracaoCampos;
}
