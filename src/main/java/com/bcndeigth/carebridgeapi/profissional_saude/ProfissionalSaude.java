package com.bcndeigth.carebridgeapi.profissional_saude;

import com.bcndeigth.carebridgeapi.users.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bcndeigth.carebridgeapi.opportunities.Oportunidade;
import com.bcndeigth.carebridgeapi.opportunities.FormularioTriagem;

@Entity
@Table(name = "profissional_saude")
@Getter
@Setter
@NoArgsConstructor
public class ProfissionalSaude {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ElementCollection
    @CollectionTable(name = "documentos_registro", joinColumns = @JoinColumn(name = "profissional_id"))
    private List<Documento> documentosRegistro = new ArrayList<>();

    @Column(name = "biografia_ou_descricao", columnDefinition = "text")
    private String biografiaOuDescricao;

    @Column(name = "validado_institucionalmente", nullable = false)
    private boolean validadoInstitucionalmente = false;

    @OneToMany(mappedBy = "ofertante", fetch = FetchType.LAZY)
    private List<Oportunidade> oportunidades = new ArrayList<>();

    @OneToMany(mappedBy = "ofertante", fetch = FetchType.LAZY)
    private List<FormularioTriagem> formulariosTriagem = new ArrayList<>();

    public ProfissionalSaude(List<Documento> documentosRegistro, String biografiaOuDescricao, Usuario usuario) {
        this.documentosRegistro = documentosRegistro;
        this.biografiaOuDescricao = biografiaOuDescricao;
        this.usuario = usuario;
    }
}
