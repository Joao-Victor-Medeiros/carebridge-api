package com.bcndeigth.carebridgeapi.opportunities;


import com.bcndeigth.carebridgeapi.paciente.Paciente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inscricoes")
@Getter
@Setter
@NoArgsConstructor
public class Inscricao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oportunidade_id", nullable = false)
    private Oportunidade oportunidade;

    @Column(nullable = false)
    private String status;

    @Column(name = "dados_respostas_triagem", columnDefinition = "text")
    private String dadosRespostasTriagem;

    @Column(name = "data_inscricao", nullable = false, updatable = false)
    private Instant dataInscricao;

    @PrePersist
    protected void aoCriar() {
        dataInscricao = Instant.now();
    }
}
