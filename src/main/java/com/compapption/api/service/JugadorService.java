package com.compapption.api.service;

import com.compapption.api.dto.jugadorDTO.JugadorDetalleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorUsuarioDTO;
import com.compapption.api.entity.Jugador;
import com.compapption.api.entity.LogModificacion;
import com.compapption.api.entity.Usuario;
import com.compapption.api.service.log.LogService;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.JugadorMapper;
import com.compapption.api.repository.JugadorRepository;
import com.compapption.api.repository.UsuarioRepository;
import com.compapption.api.request.jugador.JugadorCreateRequest;
import com.compapption.api.request.jugador.JugadorUpdateRequest;
import com.compapption.api.request.page.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final JugadorMapper jugadorMapper;
    private final LogService logService;

    // === CONSULTAS JUGADOR === //

    // Obtener jugador por id con jerarquía de detalle

    @Transactional(readOnly = true)
    public JugadorSimpleDTO obtenerPorIdSimple(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", id));

        return jugadorMapper.toSimpleDTO(jugador);
    }

    @Transactional(readOnly = true)
    public JugadorDetalleDTO obtenerPorIdDetalle(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", id));

        return jugadorMapper.toDetalleDTO(jugador);
    }

    // Búsqueda de jugador por nombre o apellidos con resultado paginado

    @Transactional(readOnly = true)
    public PageResponse<JugadorSimpleDTO> buscar(String search, Pageable pageable) {
        Page<Jugador> page = jugadorRepository.searchByNombreOrApellidos(search, pageable);
        return PageResponse.<JugadorSimpleDTO>builder()
                .content(jugadorMapper.toSimpleDTOList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<JugadorSimpleDTO> obtenerTodos() {
        return jugadorMapper.toSimpleDTOList(jugadorRepository.findAll());
    }

    // === CREAR, ELIMINAR Y ACTUALIZAR JUGADOR === //

    @Transactional
    public JugadorDetalleDTO crear(JugadorCreateRequest request) {
        Usuario usuario = null;
        if (request.getUsuarioId()!=null){
            usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", request.getUsuarioId()));

            /// Revisa si el usuario tiene perfil de jugador ya.
            /// Hay que revisar esto porque quiero que un usuario pueda ser jugador en varios equipos.
            if (jugadorRepository.findByUsuarioId(request.getUsuarioId()).isPresent()) {
                throw new BadRequestException("El usuario ya tiene un perfil de jugador");
            }
        }

        Jugador jugador = Jugador.builder()
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .dorsal(request.getDorsal())
                .posicion(request.getPosicion())
                .foto(request.getFoto())
                .usuario(usuario)
                .build();

        jugador = jugadorRepository.save(jugador);
        logService.registrar("Jugador", jugador.getId(), LogModificacion.AccionLog.CREAR, null, null, null);
        return jugadorMapper.toDetalleDTO(jugador);
    }

    @Transactional
    public JugadorDetalleDTO actualizar(Long id, JugadorUpdateRequest request) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", id));

        if (request.getNombre()!=null){
            jugador.setNombre(request.getNombre());
        }
        if (request.getApellidos()!=null){
            jugador.setApellidos(request.getApellidos());
        }
        if (request.getDorsal()!=null){
            jugador.setDorsal(request.getDorsal());
        }
        if (request.getPosicion()!=null){
            jugador.setPosicion(request.getPosicion());
        }
        if (request.getFoto()!=null){
            jugador.setFoto(request.getFoto());
        }

        jugador = jugadorRepository.save(jugador);
        logService.registrar("Jugador", jugador.getId(), LogModificacion.AccionLog.EDITAR, null, null, null);
        return jugadorMapper.toDetalleDTO(jugador);
    }

    @Transactional
    public void eliminar(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", id));

        if (!jugador.getEquipos().isEmpty()) {
            throw new BadRequestException("No se puede eliminar un jugador que pertenece a un equipo");
        }
        logService.registrar("Jugador", id, LogModificacion.AccionLog.ELIMINAR, null, null, null);
        jugadorRepository.delete(jugador);
    }

    @Transactional
    public JugadorUsuarioDTO vincularUsuario(Long jugadorId, Long usuarioId) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", jugadorId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", usuarioId));

        if (jugador.getUsuario()!=null) {
            throw new BadRequestException("El jugador ya está vinculado a un usuario");
        }

        if (jugadorRepository.findByUsuarioId(usuarioId).isPresent()) {
            throw new BadRequestException("El usuario ya tiene un perfil de jugador");
        }

        jugador.setUsuario(usuario);
        jugador = jugadorRepository.save(jugador);
        logService.registrar("Jugador", jugador.getId(), LogModificacion.AccionLog.EDITAR, null, null, null);
        return jugadorMapper.toUsuarioDTO(jugador);
    }
}
