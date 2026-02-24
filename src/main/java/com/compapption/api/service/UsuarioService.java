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

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", id));
        return usuarioMapper.toDTO(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorUsername(String username){
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "username", username));
        return usuarioMapper.toDTO(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerTodos(){
        return usuarioMapper.toDTOList(usuarioRepository.findAll());
    }

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

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void activar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }
}
