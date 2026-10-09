package com.bcndeigth.carebridgeapi.opportunities;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "oportunidades")
@Getter
@Setter
@NoArgsConstructor
public class Oportunidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ofertante_id", nullable = false)
    private ProfissionalSaude ofertante;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "tipo_servico")
    private String tipoServico;

    @Column(name = "publico_alvo", columnDefinition = "text")
    private String publicoAlvo;

    private String modalidade;

    @Column(name = "condicoes_acceso", columnDefinition = "text")
    private String condicoesAcesso;

    @Column(name = "status_ativa", nullable = false)
    private boolean statusAtiva = false;

    @Column(name = "ultima_atualizacao", nullable = false)
    private Instant ultimaAtualizacao;

    @OneToOne(mappedBy = "oportunidade", fetch = FetchType.LAZY)
    private FormularioTriagem formularioTriagem;

    @OneToMany(mappedBy = "oportunidade", fetch = FetchType.LAZY)
    private List<Inscricao> inscricoes = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void atualizarData() {
        ultimaAtualizacao = Instant.now();
    }
}
