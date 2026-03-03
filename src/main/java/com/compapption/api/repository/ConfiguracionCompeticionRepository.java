package com.compapption.api.repository;

import com.compapption.api.entity.ConfiguracionCompeticion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link ConfiguracionCompeticion}.
 * Permite recuperar la configuración de formato y puntuación asociada a una competición concreta.
 *
 * @author Mario
 */
@Repository
public interface ConfiguracionCompeticionRepository extends JpaRepository<ConfiguracionCompeticion, Long> {

    Optional<ConfiguracionCompeticion> findByCompeticionId(long competicionId);
}
