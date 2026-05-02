package com.compapption.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Representa un usuario registrado en el sistema.
 * Mapeada a la tabla {@code usuario}, almacena credenciales (username, email, password),
 * datos personales, y el flag {@code esAdminSistema} para el rol de administrador global.
 * Se relaciona con {@link UsuarioRolCompeticion} para los roles por competición,
 * con {@link Jugador} mediante una relación uno a uno, y con {@link EquipoManager}
 * para los equipos que gestiona.
 *
 * @author Mario
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false,unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellidos;

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @Column(name = "es_admin_sistema", nullable = false)
    private Boolean esAdminSistema = false;

    /**
     * Contador de intentos de login fallidos consecutivos. Se resetea a 0
     * tras un login OK o cuando se cumple {@link #bloqueadoHasta}. Cierra S-24
     * (account lockout vía {@code AuthService}).
     */
    @Builder.Default
    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos = 0;

    /**
     * Marca temporal hasta la que la cuenta está bloqueada. {@code null}
     * cuando no hay lockout activo. Cierra S-24.
     */
    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Builder.Default
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioRolCompeticion> rolesCompeticion = new HashSet<>();

    @OneToOne(mappedBy = "usuario")
    private Jugador jugador;

    @Builder.Default
    @OneToMany(mappedBy = "creador")
    private Set<Competicion> competicionesCreadas = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "usuario")
    private Set<EquipoManager> equiposGestionados = new HashSet<>();
}
