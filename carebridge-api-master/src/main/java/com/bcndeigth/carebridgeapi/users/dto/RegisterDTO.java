package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;

public record RegisterDTO(String email, String password, TipoPerfil role) {
}