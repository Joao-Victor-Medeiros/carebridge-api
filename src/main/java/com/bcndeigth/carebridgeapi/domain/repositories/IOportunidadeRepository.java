package com.bcndeigth.carebridgeapi.domain.repositories;

import com.bcndeigth.carebridgeapi.domain.entities.Oportunidade;
import java.util.List;
import java.util.Optional;

public interface IOportunidadeRepository {
    void salvar(Oportunidade oportunidade);
    Optional<Oportunidade> buscarPorId(String id);
    List<Oportunidade> buscarTodas();
}