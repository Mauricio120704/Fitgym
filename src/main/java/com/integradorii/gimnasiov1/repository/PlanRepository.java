package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    Optional<Plan> findByNombre(String nombre);
}
