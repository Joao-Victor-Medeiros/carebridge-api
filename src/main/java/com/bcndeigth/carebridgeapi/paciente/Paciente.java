package com.bcndeigth.carebridgeapi.paciente;

import com.bcndeigth.carebridgeapi.opportunities.Inscricao;
import com.bcndeigth.carebridgeapi.users.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pacientes_seq")
    @SequenceGenerator(name = "pacientes_seq", sequenceName = "pacientes_seq", allocationSize = 1)
    private Long id;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String telefone;

    @Column(columnDefinition = "text")
    private String endereco;

    @OneToOne
    @JoinColumn(
            name = "usuario_id",
            nullable =false,
            unique =true
    )
    private Usuario usuario;

    @OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY)
    private List<Inscricao> inscricoes = new ArrayList<>();

    public Paciente(LocalDate dataNascimento, String telefone, String endereco, Usuario usuario) {
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.endereco = endereco;
        this.usuario = usuario;
    }
}
