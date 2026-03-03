package com.compapption.api.repository;

import com.compapption.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Usuario.
 * Gestiona la autenticación y búsqueda de usuarios por credenciales,
 * así como la carga eager de roles y competiciones asociadas.
 *
 * @author Mario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username nombre de usuario
     * @return Optional con el usuario, vacío si no existe
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email dirección de correo electrónico
     * @return Optional con el usuario, vacío si no existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario que coincida con el nombre de usuario o el correo electrónico.
     *
     * @param username nombre de usuario
     * @param email    dirección de correo electrónico
     * @return Optional con el usuario, vacío si no existe
     */
    Optional<Usuario> findByUsernameOrEmail(String username, String email);

    /**
     * Busca un usuario por nombre de usuario o email cargando en la misma consulta
     * sus roles de competición. Usado en el proceso de login.
     *
     * @param usernameOrEmail nombre de usuario o dirección de correo electrónico
     * @return Optional con el usuario y sus roles, vacío si no existe
     */
    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.rolesCompeticion rc " +
            "LEFT JOIN FETCH rc.rol " +
            "WHERE u.username = :usernameOrEmail " +
            "OR u.email = :usernameOrEmail")
    Optional<Usuario> findByUsernameOrEmailWithRoles(
            @Param("usernameOrEmail") String usernameOrEmail
    );

    /**
     * Comprueba si ya existe un usuario con el nombre de usuario indicado.
     *
     * @param username nombre de usuario a verificar
     * @return {@code true} si el nombre de usuario ya está en uso
     */
    boolean existsByUsername(String username);

    /**
     * Comprueba si ya existe un usuario con el correo electrónico indicado.
     *
     * @param email dirección de correo electrónico a verificar
     * @return {@code true} si el correo ya está registrado
     */
    boolean existsByEmail(String email);

    /**
     * Comprueba si existe otro usuario con el mismo email, excluyendo al usuario con el id dado.
     * Útil para validar unicidad de email en actualizaciones de perfil.
     *
     * @param email dirección de correo electrónico
     * @param id    identificador del usuario a excluir de la comprobación
     * @return {@code true} si otro usuario ya usa ese correo
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Busca un usuario por nombre de usuario cargando en la misma consulta
     * sus roles de competición.
     *
     * @param username nombre de usuario
     * @return Optional con el usuario y sus roles, vacío si no existe
     */
    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.rolesCompeticion rc " +
            "LEFT JOIN FETCH rc.rol " +
            "WHERE u.username = :username")
    Optional<Usuario> findByusernameWithRoles(
            @Param("username") String username
    );

    /**
     * Busca un usuario por su identificador cargando en la misma consulta
     * sus roles y las competiciones asociadas a cada rol.
     *
     * @param id identificador del usuario
     * @return Optional con el usuario, sus roles y competiciones, vacío si no existe
     */
    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.rolesCompeticion rc " +
            "LEFT JOIN FETCH rc.rol " +
            "LEFT JOIN FETCH rc.competicion " +
            "WHERE u.id = :id")
    Optional<Usuario> findByIdWithRolesAndCompeticiones(
            @Param("id") long id
    );
}
