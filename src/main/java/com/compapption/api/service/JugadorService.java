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

/**
 * Servicio de gestión de jugadores.
 *
 * <p>Ofrece operaciones CRUD sobre la entidad {@code Jugador} (consulta simple y
 * detalle, búsqueda paginada, creación, actualización y eliminación) además de la
 * operación de vinculación de un jugador con un {@code Usuario} del sistema.
 * Todas las mutaciones quedan registradas en el log de auditoría a través de
 * {@code LogService}.</p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final JugadorMapper jugadorMapper;
    private final LogService logService;

    // === CONSULTAS JUGADOR === //

    /**
     * Obtiene un jugador en formato simple por su identificador.
     *
     * @param id identificador del jugador
     * @return {@link JugadorSimpleDTO} con los campos básicos del jugador
     * @throws ResourceNotFoundException si no existe ningún jugador con ese id
     */
    @Transactional(readOnly = true)
    public JugadorSimpleDTO obtenerPorIdSimple(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", id));

        return jugadorMapper.toSimpleDTO(jugador);
    }

    /**
     * Obtiene un jugador en formato detalle por su identificador.
     *
     * @param id identificador del jugador
     * @return {@link JugadorDetalleDTO} con todos los datos del jugador
     * @throws ResourceNotFoundException si no existe ningún jugador con ese id
     */
    @Transactional(readOnly = true)
    public JugadorDetalleDTO obtenerPorIdDetalle(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", id));

        return jugadorMapper.toDetalleDTO(jugador);
    }

    /**
     * Realiza una búsqueda paginada de jugadores por nombre o apellidos.
     *
     * @param search   cadena a buscar en nombre o apellidos
     * @param pageable parámetros de paginación y ordenación
     * @return {@link PageResponse} con la página de {@link JugadorSimpleDTO} resultante
     */
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

    /**
     * Devuelve la lista completa de jugadores registrados en el sistema.
     *
     * @return lista de {@link JugadorSimpleDTO} con todos los jugadores
     */
    @Transactional(readOnly = true)
    public List<JugadorSimpleDTO> obtenerTodos() {
        return jugadorMapper.toSimpleDTOList(jugadorRepository.findAll());
    }

    // === CREAR, ELIMINAR Y ACTUALIZAR JUGADOR === //

    /**
     * Crea un nuevo jugador y opcionalmente lo vincula a un usuario existente.
     *
     * <p>Si se proporciona {@code usuarioId}, verifica que el usuario exista y que no
     * tenga ya un perfil de jugador asociado antes de vincularlos.</p>
     *
     * @param request datos del jugador (nombre, apellidos, dorsal, posición, foto y usuarioId opcional)
     * @return {@link JugadorDetalleDTO} con el jugador recién creado
     * @throws ResourceNotFoundException si se indica un usuarioId que no existe
     * @throws BadRequestException       si el usuario ya tiene un perfil de jugador
     */
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

    /**
     * Actualiza los datos de un jugador existente.
     *
     * <p>Solo se modifican los campos no nulos del request.</p>
     *
     * @param id      identificador del jugador a actualizar
     * @param request campos a actualizar (nombre, apellidos, dorsal, posición y/o foto)
     * @return {@link JugadorDetalleDTO} con los datos actualizados
     * @throws ResourceNotFoundException si no existe ningún jugador con ese id
     */
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

    /**
     * Elimina un jugador del sistema tras verificar que no pertenece a ningún equipo.
     *
     * @param id identificador del jugador a eliminar
     * @throws ResourceNotFoundException si no existe ningún jugador con ese id
     * @throws BadRequestException       si el jugador pertenece a uno o más equipos
     */
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

    /**
     * Vincula un jugador existente a un usuario del sistema.
     *
     * <p>Verifica que el jugador no tenga ya un usuario vinculado y que el usuario
     * no tenga ya otro perfil de jugador antes de establecer la relación.</p>
     *
     * @param jugadorId identificador del jugador
     * @param usuarioId identificador del usuario a vincular
     * @return {@link JugadorUsuarioDTO} con la información del jugador y su usuario vinculado
     * @throws ResourceNotFoundException si el jugador o el usuario no existen
     * @throws BadRequestException       si el jugador ya está vinculado a un usuario o el usuario ya tiene perfil de jugador
     */
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
