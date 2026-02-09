package com.compapption.api.repository;

import com.compapption.api.entity.ConfiguracionCompeticion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionCompeticionRepository extends JpaRepository<ConfiguracionCompeticion, Long> {

    Optional<ConfiguracionCompeticion> findByCompeticionId(Long competicionId);
}
