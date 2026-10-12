package com.bcndeigth.carebridgeapi.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<Usuario, Long> {
}
