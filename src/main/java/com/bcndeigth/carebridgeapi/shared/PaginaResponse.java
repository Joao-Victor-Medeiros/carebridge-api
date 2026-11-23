package com.bcndeigth.carebridgeapi.shared;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PaginaResponse<T>(List<T> content, int number, int size, int totalPages, long totalElements) {

    public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<E, T> mapear) {
        return new PaginaResponse<>(pagina.getContent().stream().map(mapear).toList(), pagina.getNumber(),
                pagina.getSize(), Math.max(1, pagina.getTotalPages()), pagina.getTotalElements());
    }
}
