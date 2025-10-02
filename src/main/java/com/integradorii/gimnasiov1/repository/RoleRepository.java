package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByCodigo(String codigo);
}
