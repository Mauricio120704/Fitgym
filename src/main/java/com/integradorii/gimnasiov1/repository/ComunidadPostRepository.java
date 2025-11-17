package com.integradorii.gimnasiov1.repository;

import com.integradorii.gimnasiov1.model.ComunidadPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComunidadPostRepository extends JpaRepository<ComunidadPost, Long> {

    List<ComunidadPost> findTop50ByActivoTrueOrderByCreadoEnDesc();
}
