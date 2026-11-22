package com.bcndeigth.carebridgeapi.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegistroTentativaVerificacaoService {
    private final CodigoVerificacaoEmailRepository codigoRepository;

    public RegistroTentativaVerificacaoService(CodigoVerificacaoEmailRepository codigoRepository) {
        this.codigoRepository = codigoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int registrarFalha(UUID codigoId) {
        CodigoVerificacaoEmail codigo = codigoRepository.findById(codigoId)
                .orElseThrow();
        codigo.setTentativas(codigo.getTentativas() + 1);
        codigoRepository.save(codigo);
        return codigo.getTentativas();
    }
}