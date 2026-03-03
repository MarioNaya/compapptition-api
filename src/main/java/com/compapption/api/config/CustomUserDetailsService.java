package com.compapption.api.config;

import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de {@link UserDetailsService} que carga los datos del usuario
 * desde la base de datos a partir de su nombre de usuario.
 *
 * <p>Es utilizada por el {@link SecurityConfig#authenticationProvider()} (flujo de login)
 * para obtener el usuario durante la autenticación con credenciales (email/username + contraseña).
 * En el flujo de peticiones autenticadas con JWT, el usuario se construye directamente desde
 * los claims del token sin pasar por este servicio.
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga el usuario de la base de datos a partir de su nombre de usuario.
     *
     * <p>Busca en {@code UsuarioRepository} por username y mapea la entidad
     * {@code Usuario} a un {@link CustomUserDetails}. La transacción garantiza que
     * las colecciones lazy (si las hubiera) se carguen dentro del contexto de persistencia.
     *
     * @param username el nombre de usuario con el que se intenta autenticar.
     * @return un {@link CustomUserDetails} con los datos del usuario encontrado.
     * @throws UsernameNotFoundException si no existe ningún usuario con ese username.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(()-> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }
}
