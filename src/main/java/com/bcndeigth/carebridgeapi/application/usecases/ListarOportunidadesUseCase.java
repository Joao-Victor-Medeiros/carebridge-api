package com.bcndeigth.carebridgeapi.application.usecases;

import com.bcndeigth.carebridgeapi.domain.entities.Oportunidade;
import com.bcndeigth.carebridgeapi.domain.repositories.IOportunidadeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListarOportunidadesUseCase {

    private final IOportunidadeRepository repository;

    public ListarOportunidadesUseCase(IOportunidadeRepository repository) {
        this.repository = repository;
    }

    public List<Oportunidade> executar() {
        return repository.buscarTodas();
    }
}