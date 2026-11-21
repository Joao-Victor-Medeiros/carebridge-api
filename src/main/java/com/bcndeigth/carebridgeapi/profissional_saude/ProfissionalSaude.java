package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.oportunidade.Oportunidade;
import com.bcndeigth.carebridgeapi.users.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profissional_saude")
@Getter
@Setter
@NoArgsConstructor
public class ProfissionalSaude {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @Column(nullable = false)
    private String nome;

    @Column(name = "conselho", length = 50)
    private String conselho;

    @Column(name = "uf", length = 2)
    private String uf;

    @Column(name = "numero_registro", length = 50)
    private String numeroRegistro;

    @Column(name = "especialidade", length = 150)
    private String especialidade;

    @Column(name = "onde_atende", length = 255)
    private String ondeAtende;

    @ElementCollection
    @CollectionTable(
            name = "documentos_registro",
            joinColumns = @JoinColumn(name = "profissional_id")
    )
    private List<Documento> documentosRegistro = new ArrayList<>();

    @Column(name = "biografia_ou_descricao", columnDefinition = "text")
    private String biografiaOuDescricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_registro", nullable = false, length = 20)
    private SituacaoRegistro situacaoRegistro = SituacaoRegistro.EM_VERIFICACAO;

    @OneToMany(mappedBy = "ofertante", fetch = FetchType.LAZY)
    private List<Oportunidade> oportunidades = new ArrayList<>();

    public ProfissionalSaude(
            String nome,
            String conselho,
            String uf,
            String numeroRegistro,
            String especialidade,
            String ondeAtende,
            List<Documento> documentosRegistro,
            String biografiaOuDescricao,
            Usuario usuario
    ) {
        this.nome = nome;
        this.conselho = conselho;
        this.uf = uf;
        this.numeroRegistro = numeroRegistro;
        this.especialidade = especialidade;
        this.ondeAtende = ondeAtende;
        this.documentosRegistro = documentosRegistro;
        this.biografiaOuDescricao = biografiaOuDescricao;
        this.usuario = usuario;
    }
}