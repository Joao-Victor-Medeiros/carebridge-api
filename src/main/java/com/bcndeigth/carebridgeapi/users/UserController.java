package com.bcndeigth.carebridgeapi.users;

import com.bcndeigth.carebridgeapi.users.dto.SignUpUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public void signUp(@RequestBody SignUpUserDto dto) {
        //aqui deve registrar segundo sua role
        userService.cadastrarUsuario(dto);
    }
}
