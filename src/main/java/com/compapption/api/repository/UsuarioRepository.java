package com.compapption.api.repository;

import com.compapption.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUsernameOrEmail(String username, String email);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.rolesCompeticion rc " +
            "LEFT JOIN FETCH rc.rol " +
            "WHERE u.username = :usernameOrEmail " +
            "OR u.email = :usernameOrEmail")
    Optional<Usuario> findByUsernameOrEmailWithRoles(
            @Param("usernameOrEmail") String usernameOrEmail
    );

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.rolesCompeticion rc " +
            "LEFT JOIN FETCH rc.rol " +
            "WHERE u.username = :username")
    Optional<Usuario> findByusernameWithRoles(
            @Param("username") String username
    );

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.rolesCompeticion rc " +
            "LEFT JOIN FETCH rc.rol " +
            "LEFT JOIN FETCH rc.competicion " +
            "WHERE u.id = :id")
    Optional<Usuario> findByIdWithRolesAndCompeticiones(
            @Param("id") long id
    );
}
