package com.compapption.api.repository;

import com.compapption.api.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Rol.
 * Gestiona la búsqueda y verificación de roles del sistema
 * (ADMIN_SISTEMA, ADMIN_COMPETICION, MANAGER_EQUIPO, JUGADOR).
 *
 * @author Mario
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre de enum.
     *
     * @param nombre nombre del rol según {@link Rol.RolNombre}
     * @return Optional con el rol, vacío si no existe
     */
    Optional<Rol> findByNombre(Rol.RolNombre nombre);

    /**
     * Comprueba si existe un rol con el nombre indicado.
     *
     * @param nombre nombre del rol según {@link Rol.RolNombre}
     * @return {@code true} si el rol existe en base de datos
     */
    boolean existsByNombre(Rol.RolNombre nombre);
}
