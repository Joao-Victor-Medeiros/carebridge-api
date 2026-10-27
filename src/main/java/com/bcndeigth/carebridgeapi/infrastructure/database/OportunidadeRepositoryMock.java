package com.bcndeigth.carebridgeapi.infrastructure.database;

import com.bcndeigth.carebridgeapi.domain.entities.Oportunidade;
import com.bcndeigth.carebridgeapi.domain.repositories.IOportunidadeRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OportunidadeRepositoryMock implements IOportunidadeRepository {

    private final List<Oportunidade> banco = new ArrayList<>();

    public OportunidadeRepositoryMock() {
        // Criando os dados falsos exatamente como o iOS precisa
        banco.add(new Oportunidade("op-001", "pr-001", "online", "gratuita", 3));
        banco.add(new Oportunidade("op-002", "pr-002", "presencial", "social", 8));
    }

    @Override
    public void salvar(Oportunidade oportunidade) {
        banco.add(oportunidade);
    }

    @Override
    public Optional<Oportunidade> buscarPorId(String id) {
        return banco.stream()
                .filter(op -> op.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Oportunidade> buscarTodas() {
        return banco;
    }
}