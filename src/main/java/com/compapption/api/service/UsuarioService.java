package com.compapption.api.service;

import com.compapption.api.dto.usuario.UsuarioDTO;
import com.compapption.api.entity.Usuario;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.UsuarioMapper;
import com.compapption.api.repository.UsuarioRepository;
import com.compapption.api.request.usuario.UsuarioUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de gestión de usuarios del sistema.
 *
 * <p>Proporciona operaciones CRUD sobre la entidad {@code Usuario}: consulta por id
 * o username, actualización de datos de perfil, cambio de contraseña y
 * activación/desactivación de la cuenta.</p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene un usuario por su identificador único.
     *
     * @param id identificador del usuario
     * @return {@link UsuarioDTO} con los datos del usuario
     * @throws ResourceNotFoundException si no existe ningún usuario con ese id
     */
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", id));
        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Obtiene un usuario por su nombre de usuario.
     *
     * @param username nombre de usuario
     * @return {@link UsuarioDTO} con los datos del usuario
     * @throws ResourceNotFoundException si no existe ningún usuario con ese username
     */
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorUsername(String username){
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "username", username));
        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Devuelve la lista completa de usuarios registrados en el sistema.
     *
     * @return lista de {@link UsuarioDTO} con todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerTodos(){
        return usuarioMapper.toDTOList(usuarioRepository.findAll());
    }

    /**
     * Actualiza los datos de perfil de un usuario (nombre, apellidos y/o email).
     *
     * <p>Solo se modifican los campos no nulos del request. Si se incluye un nuevo email,
     * se verifica que no esté ya en uso por otro usuario.</p>
     *
     * @param id      identificador del usuario a actualizar
     * @param request campos a actualizar (todos opcionales)
     * @return {@link UsuarioDTO} con los datos actualizados
     * @throws ResourceNotFoundException si no existe ningún usuario con ese id
     * @throws BadRequestException       si el nuevo email ya está en uso por otro usuario
     */
    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", id));

        if (request.getNombre()!=null) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getApellidos()!=null) {
            usuario.setApellidos(request.getApellidos());
        }
        if (request.getEmail()!=null) {
            if (usuarioRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new BadRequestException("El email ya está en uso");
            }
            usuario.setEmail(request.getEmail());
        }

        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.toDTO(usuario);
    }

    /**
     * Cambia la contraseña de un usuario verificando previamente la contraseña actual.
     *
     * @param id              identificador del usuario
     * @param passwordActual  contraseña actual en texto plano para verificación
     * @param passwordNuevo   nueva contraseña en texto plano (se almacenará codificada)
     * @throws ResourceNotFoundException si no existe ningún usuario con ese id
     * @throws BadRequestException       si la contraseña actual no coincide
     */
    @Transactional
    public void cambiarPassword(Long id, String passwordActual, String passwordNuevo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", id));

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNuevo));
        usuarioRepository.save(usuario);
    }

    /**
     * Desactiva la cuenta de un usuario impidiendo futuros inicios de sesión.
     *
     * @param id identificador del usuario a desactivar
     * @throws ResourceNotFoundException si no existe ningún usuario con ese id
     */
    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Reactiva la cuenta de un usuario previamente desactivada.
     *
     * @param id identificador del usuario a activar
     * @throws ResourceNotFoundException si no existe ningún usuario con ese id
     */
    @Transactional
    public void activar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
}
