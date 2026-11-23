package com.bcndeigth.carebridgeapi.oportunidade;

import com.bcndeigth.carebridgeapi.profissional_saude.ProfissionalSaude;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "oportunidades")
@Getter
@Setter
@NoArgsConstructor
public class Oportunidade {

    public static final int PRAZO_RESPOSTA_PADRAO = 15;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servico")
    private TipoServico tipoServico;

    @Column(length = 60)
    private String especialidade;

    @Enumerated(EnumType.STRING)
    private Modalidade modalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade_acesso", length = 20)
    private ModalidadeAcesso modalidadeAcesso;

    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    @Convert(converter = PublicoAlvoConverter.class)
    @Column(name = "publico_alvo", columnDefinition = "text")
    private List<PublicoAlvo> publicoAlvo = new ArrayList<>();

    @Getter(lombok.AccessLevel.NONE)
    @Setter(lombok.AccessLevel.NONE)
    @Column(name = "publico_alvo", insertable = false, updatable = false)
    private String publicoAlvoTexto;

    @Column(name = "publico_alvo_observacao", columnDefinition = "text")
    private String publicoAlvoObservacao;

    @Column(name = "vagas_totais")
    private Integer vagasTotais;

    @Column(name = "vagas_preenchidas", nullable = false)
    private int vagasPreenchidas;

    @Column(length = 9)
    private String cep;

    private String endereco;

    @Column(length = 120)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(name = "inscricoes_de")
    private LocalDate inscricoesDe;

    @Column(name = "inscricoes_ate")
    private LocalDate inscricoesAte;

    @Column(name = "criterios_elegibilidade", columnDefinition = "text")
    private String criteriosElegibilidade;

    @Column(name = "sem_restricoes", nullable = false)
    private boolean semRestricoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_contato", length = 20)
    private FormaContato formaContato;

    @Column(name = "prazo_resposta_dias", nullable = false)
    private int prazoRespostaDias = PRAZO_RESPOSTA_PADRAO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOportunidade status = StatusOportunidade.RASCUNHO;

    @Column(name = "ultima_atualizacao", nullable = false)
    private Instant ultimaAtualizacao;

    @OneToOne(mappedBy = "oportunidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private FormularioTriagem formularioTriagem;

    @Formula("(select count(*) from inscricoes i where i.oportunidade_id = id)")
    private int totalInscricoes;

    public boolean pertenceA(UUID usuarioId) {
        return ofertante.getId().equals(usuarioId);
    }

    public void definirTriagem(FormularioTriagem triagem) {
        if (formularioTriagem != null) {
            formularioTriagem.setOportunidade(null);
        }
        formularioTriagem = triagem;
        if (triagem != null) {
            triagem.setOportunidade(this);
        }
    }

    @PrePersist
    @PreUpdate
    protected void atualizarData() {
        ultimaAtualizacao = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
