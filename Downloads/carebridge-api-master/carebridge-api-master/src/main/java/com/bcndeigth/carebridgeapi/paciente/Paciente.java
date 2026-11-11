package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.oportunidade.Inscricao;
import com.bcndeigth.carebridgeapi.users.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
public class Paciente {
    
    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String telefone;

    @Column(columnDefinition = "text")
    private String endereco;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId 
    @JoinColumn(
            name = "usuario_id",
            nullable = false,
            unique = true
    )
    private Usuario usuario;

    @OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY)
    private List<Inscricao> inscricoes = new ArrayList<>();

    public Paciente(String nome, LocalDate dataNascimento, String telefone, String endereco, Usuario usuario) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.endereco = endereco;
        this.usuario = usuario;
    }
}