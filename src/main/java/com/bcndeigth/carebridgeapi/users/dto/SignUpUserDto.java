package com.bcndeigth.carebridgeapi.users.dto;

import com.bcndeigth.carebridgeapi.users.TipoPerfil;

public record SignUpUserDto(String nome, String email, String senha, TipoPerfil tipoPerfil){
}