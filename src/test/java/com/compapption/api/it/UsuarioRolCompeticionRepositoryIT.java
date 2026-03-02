package com.compapption.api.it;

import com.compapption.api.entity.*;
import com.compapption.api.repository.*;
import com.compapption.api.util.BaseRepositoryIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioRolCompeticionRepositoryIT extends BaseRepositoryIT {

    @Autowired private UsuarioRolCompeticionRepository urcRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CompeticionRepository competicionRepository;
    @Autowired private DeporteRepository deporteRepository;
    @Autowired private RolRepository rolRepository;

    private Usuario usuario;
    private Competicion competicion;
    private Rol rolAdmin;
    private Rol rolJugador;

    @BeforeEach
    void setUp() {
        Deporte deporte = deporteRepository.save(Deporte.builder().nombre("Fútbol").build());
        Usuario creador = usuarioRepository.save(
                Usuario.builder().username("creador").email("creador@test.com").password("hash").build());
        competicion = competicionRepository.save(
                Competicion.builder().nombre("Liga").deporte(deporte).creador(creador).build());
        usuario = usuarioRepository.save(
                Usuario.builder().username("jugador").email("jugador@test.com").password("hash").build());
        // findOrCreate: los roles pueden existir ya en la BD (creados por otros tests sin rollback)
        rolAdmin = rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPETICION)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(Rol.RolNombre.ADMIN_COMPETICION).build()));
        rolJugador = rolRepository.findByNombre(Rol.RolNombre.JUGADOR)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(Rol.RolNombre.JUGADOR).build()));
    }

    // =========================================================
    // existsByUsuarioIdAndCompeticionIdAndRolNombre
    // =========================================================

    @Test
    void existsByRolNombre_trueWhenExists() {
        urcRepository.save(UsuarioRolCompeticion.builder()
                .usuario(usuario).competicion(competicion).rol(rolAdmin).build());

        boolean result = urcRepository.existsByUsuarioIdAndCompeticionIdAndRolNombre(
                usuario.getId(), competicion.getId(), Rol.RolNombre.ADMIN_COMPETICION);

        assertThat(result).isTrue();
    }

    @Test
    void existsByRolNombre_falseWhenNotExists() {
        boolean result = urcRepository.existsByUsuarioIdAndCompeticionIdAndRolNombre(
                usuario.getId(), competicion.getId(), Rol.RolNombre.ADMIN_COMPETICION);

        assertThat(result).isFalse();
    }

    @Test
    void existsByRolNombre_falseWhenDifferentRole() {
        urcRepository.save(UsuarioRolCompeticion.builder()
                .usuario(usuario).competicion(competicion).rol(rolJugador).build());

        boolean result = urcRepository.existsByUsuarioIdAndCompeticionIdAndRolNombre(
                usuario.getId(), competicion.getId(), Rol.RolNombre.ADMIN_COMPETICION);

        assertThat(result).isFalse();
    }

    // =========================================================
    // existsByUsuarioIdAndCompeticionIdAndRolNombreIn
    // =========================================================

    @Test
    void existsByRolNombreIn_trueWhenOneRoleMatches() {
        urcRepository.save(UsuarioRolCompeticion.builder()
                .usuario(usuario).competicion(competicion).rol(rolJugador).build());

        boolean result = urcRepository.existsByUsuarioIdAndCompeticionIdAndRolNombreIn(
                usuario.getId(), competicion.getId(),
                List.of(Rol.RolNombre.ADMIN_COMPETICION, Rol.RolNombre.JUGADOR));

        assertThat(result).isTrue();
    }

    @Test
    void existsByRolNombreIn_falseWhenNoRoleMatches() {
        boolean result = urcRepository.existsByUsuarioIdAndCompeticionIdAndRolNombreIn(
                usuario.getId(), competicion.getId(),
                List.of(Rol.RolNombre.ADMIN_COMPETICION, Rol.RolNombre.JUGADOR));

        assertThat(result).isFalse();
    }

    // =========================================================
    // existsByUsuarioIdAndCompeticionId
    // =========================================================

    @Test
    void existsByUsuarioIdAndCompeticionId_trueWhenHasAnyRole() {
        urcRepository.save(UsuarioRolCompeticion.builder()
                .usuario(usuario).competicion(competicion).rol(rolJugador).build());

        boolean result = urcRepository.existsByUsuarioIdAndCompeticionId(
                usuario.getId(), competicion.getId());

        assertThat(result).isTrue();
    }

    // =========================================================
    // findByCompeticionId — LEFT JOIN FETCH de usuario y rol
    // =========================================================

    @Test
    void findByCompeticionId_devuelveConUsuarioYRolCargados() {
        urcRepository.save(UsuarioRolCompeticion.builder()
                .usuario(usuario).competicion(competicion).rol(rolAdmin).build());

        List<UsuarioRolCompeticion> result = urcRepository.findByCompeticionId(competicion.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsuario().getUsername()).isEqualTo("jugador");
        assertThat(result.get(0).getRol().getNombre()).isEqualTo(Rol.RolNombre.ADMIN_COMPETICION);
    }

    // =========================================================
    // findByUsuarioIdWithRolesAndCompeticiones — usado en JWT
    // =========================================================

    @Test
    void findByUsuarioIdWithRolesAndCompeticiones_devuelveTodosLosRoles() {
        Competicion c2 = competicionRepository.save(
                Competicion.builder().nombre("Copa").deporte(
                        deporteRepository.findAll().get(0)).creador(
                        usuarioRepository.findById(competicion.getCreador().getId()).orElseThrow()).build());

        urcRepository.save(UsuarioRolCompeticion.builder()
                .usuario(usuario).competicion(competicion).rol(rolAdmin).build());
        urcRepository.save(UsuarioRolCompeticion.builder()
                .usuario(usuario).competicion(c2).rol(rolJugador).build());

        List<UsuarioRolCompeticion> result = urcRepository
                .findByUsuarioIdWithRolesAndCompeticiones(usuario.getId());

        assertThat(result).hasSize(2);
    }
}
