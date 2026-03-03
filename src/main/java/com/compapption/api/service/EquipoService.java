package com.compapption.api.service;

import com.compapption.api.dto.equipoDTO.EquipoDetalleDTO;
import com.compapption.api.dto.equipoDTO.EquipoSimpleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorDetalleDTO;
import com.compapption.api.dto.jugadorDTO.JugadorSimpleDTO;
import com.compapption.api.entity.*;
import com.compapption.api.exception.BadRequestException;
import com.compapption.api.exception.ResourceNotFoundException;
import com.compapption.api.mapper.EquipoMapper;
import com.compapption.api.mapper.JugadorMapper;
import com.compapption.api.repository.*;
import com.compapption.api.request.equipo.EquipoCreateRequest;
import com.compapption.api.request.equipo.EquipoUpdateRequest;
import com.compapption.api.request.page.PageResponse;
import com.compapption.api.entity.LogModificacion;
import com.compapption.api.service.log.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de gestión de equipos deportivos.
 *
 * <p>Cubre el ciclo de vida completo de un equipo: creación, consulta (simple y
 * detalle, paginada por nombre), actualización, eliminación y las operaciones de
 * composición del plantel (agregar/quitar jugadores) y asignación de managers por
 * competición. Todas las mutaciones quedan registradas en el log de auditoría a
 * través de {@code LogService}.</p>
 *
 * @author Mario
 */
@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final JugadorRepository jugadorRepository;
    private final EquipoJugadorRepository equipoJugadorRepository;
    private final EquipoManagerRepository equipoManagerRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoMapper equipoMapper;
    private final JugadorMapper jugadorMapper;
    private final LogService logService;

    // === CONSULTAS EQUIPO === //

    /**
     * Devuelve todos los equipos en formato detalle (incluye jugadores y relaciones).
     *
     * @return lista de {@link EquipoDetalleDTO} con todos los equipos
     */
    @Transactional(readOnly = true)
    public List<EquipoDetalleDTO> obtenerTodosDetalle() {
        return equipoRepository.findAll()
                .stream()
                .map(equipoMapper::toDetalleDTO)
                .toList();
    }

    /**
     * Devuelve todos los equipos en formato simple (campos mínimos, sin relaciones pesadas).
     *
     * @return lista de {@link EquipoSimpleDTO} con todos los equipos
     */
    @Transactional(readOnly = true)
    public List<EquipoSimpleDTO> obtenerTodosSimple() {
        return equipoRepository.findAll()
                .stream()
                .map(equipoMapper::toSimpleDTO)
                .toList();
    }

    /**
     * Obtiene un equipo en formato detalle (con jugadores) por su identificador.
     *
     * @param id identificador del equipo
     * @return {@link EquipoDetalleDTO} con todos los datos del equipo
     * @throws ResourceNotFoundException si no existe ningún equipo con ese id
     */
    @Transactional(readOnly = true)
    public EquipoDetalleDTO obtenerPorIdDetalle(Long id) {
        Equipo equipo = equipoRepository.findByIdWithJugadores(id)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", id));
        return equipoMapper.toDetalleDTO(equipo);
    }

    /**
     * Obtiene un equipo en formato simple por su identificador.
     *
     * @param id identificador del equipo
     * @return {@link EquipoSimpleDTO} con los campos básicos del equipo
     * @throws ResourceNotFoundException si no existe ningún equipo con ese id
     */
    @Transactional(readOnly = true)
    public EquipoSimpleDTO obtenerPorIdSimple(Long id) {
        Equipo equipo = equipoRepository.findByIdWithJugadores(id)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", id));
        return equipoMapper.toSimpleDTO(equipo);
    }

    /**
     * Realiza una búsqueda paginada de equipos por nombre.
     *
     * @param nombre   cadena a buscar (puede ser parcial o nula para devolver todos)
     * @param pageable parámetros de paginación y ordenación
     * @return {@link PageResponse} con la página de {@link EquipoSimpleDTO} resultante
     */
    @Transactional(readOnly = true)
    public PageResponse<EquipoSimpleDTO> buscar(String nombre, Pageable pageable){
        Page<Equipo> page = equipoRepository.searchByNombre(nombre, pageable);
        return PageResponse.<EquipoSimpleDTO>builder()
                .content(page.getContent().stream().map(equipoMapper::toSimpleDTO).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * Devuelve los equipos en los que un usuario ejerce como manager.
     *
     * @param usuarioId identificador del usuario manager
     * @return lista de {@link EquipoSimpleDTO} gestionados por el usuario
     */
    @Transactional(readOnly = true)
    public List<EquipoSimpleDTO> obtenerPorManager(Long usuarioId){
        return equipoRepository.findByManagersId(usuarioId)
                .stream()
                .map(equipoMapper::toSimpleDTO)
                .toList();
    }

    /**
     * Devuelve los equipos a los que pertenece un jugador vinculado a un usuario.
     *
     * @param usuarioId identificador del usuario jugador
     * @return lista de {@link EquipoSimpleDTO} en los que participa el jugador
     */
    @Transactional(readOnly = true)
    public List<EquipoSimpleDTO> obtenerPorJugador(Long usuarioId){
        return equipoRepository.findByJugadoresId(usuarioId)
                .stream()
                .map(equipoMapper::toSimpleDTO)
                .toList();
    }

    // === CREAR, ELIMINAR Y ACTUALIZAR EQUIPO === //

    /**
     * Crea un nuevo equipo y registra la acción en el log de auditoría.
     *
     * @param request datos del equipo a crear (nombre, descripción, escudo)
     * @return {@link EquipoDetalleDTO} con el equipo recién creado
     */
    @Transactional
    public EquipoDetalleDTO crear(EquipoCreateRequest request) {
        Equipo equipo = Equipo.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .escudo(request.getEscudo())
                .build();

        equipo = equipoRepository.save(equipo);
        logService.registrar("Equipo", equipo.getId(), LogModificacion.AccionLog.CREAR, null, null, null);
        return equipoMapper.toDetalleDTO(equipo);
    }

    /**
     * Elimina un equipo del sistema tras verificar que no esté inscrito en ninguna competición.
     *
     * @param id identificador del equipo a eliminar
     * @throws ResourceNotFoundException si no existe ningún equipo con ese id
     * @throws BadRequestException       si el equipo está inscrito en alguna competición activa
     */
    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", id));

        if (!equipo.getCompeticiones().isEmpty()) {
            throw new BadRequestException("No se puede eliminar un equipo inscrito en competiciones");
        }

        logService.registrar("Equipo", id, LogModificacion.AccionLog.ELIMINAR, null, null, null);
        equipoRepository.delete(equipo);
    }

    /**
     * Actualiza los datos de un equipo (nombre, descripción y/o escudo).
     *
     * <p>Solo se modifican los campos no nulos del request.</p>
     *
     * @param id      identificador del equipo a actualizar
     * @param request campos a actualizar (todos opcionales)
     * @return {@link EquipoSimpleDTO} con los datos actualizados
     * @throws ResourceNotFoundException si no existe ningún equipo con ese id
     */
    @Transactional
    public EquipoSimpleDTO actualizar(Long id, EquipoUpdateRequest request) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", id));

        if (request.getNombre()!=null){
            equipo.setNombre(request.getNombre());
        }
        if (request.getDescripcion()!=null){
            equipo.setDescripcion(request.getDescripcion());
        }
        if (request.getEscudo()!=null){
            equipo.setEscudo(request.getEscudo());
        }

        equipo = equipoRepository.save(equipo);
        logService.registrar("Equipo", equipo.getId(), LogModificacion.AccionLog.EDITAR, null, null, null);
        return equipoMapper.toSimpleDTO(equipo);
    }

    // === GESTIÓN DE USUARIOS === //

    /**
     * Agrega un jugador a un equipo con un dorsal específico.
     *
     * <p>Verifica que el jugador no pertenezca ya al equipo (relación activa) y que
     * el dorsal no esté ocupado por otro jugador en ese equipo.</p>
     *
     * @param equipoId  identificador del equipo
     * @param jugadorId identificador del jugador
     * @param dorsal    número de dorsal a asignar en el equipo
     * @throws ResourceNotFoundException si el equipo o el jugador no existen
     * @throws BadRequestException       si el jugador ya pertenece al equipo o el dorsal está en uso
     */
    @Transactional
    public void agregarJugador(Long equipoId, Long jugadorId, Integer dorsal) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(()-> new ResourceNotFoundException("Equipo", "id", equipoId));

        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(()-> new ResourceNotFoundException("Jugador", "id", jugadorId));

        if (equipoJugadorRepository.existsByEquipoIdAndJugadorIdAndActivoTrue(equipoId, jugadorId)) {
            throw new BadRequestException("El jugador ya pertenece a este equipo");
        }

        if (equipoJugadorRepository.findByEquipoIdAndDorsalEquipo(equipoId, dorsal).isPresent()){
            throw new BadRequestException("El dorsal " + dorsal + " ya está asignado");
        }

        EquipoJugador equipoJugador = EquipoJugador.builder()
                .equipo(equipo)
                .jugador(jugador)
                .dorsalEquipo(dorsal)
                .build();

        equipoJugadorRepository.save(equipoJugador);
        logService.registrar("EquipoJugador", jugadorId, LogModificacion.AccionLog.CREAR, null, null, null);
    }

    /**
     * Da de baja a un jugador de un equipo (soft-delete: marca la relación como inactiva).
     *
     * @param equipoId  identificador del equipo
     * @param jugadorId identificador del jugador
     * @throws ResourceNotFoundException si el jugador no pertenece al equipo
     */
    @Transactional
    public void quitarJugador(Long equipoId, Long jugadorId) {
        EquipoJugador equipoJugador = equipoJugadorRepository
                .findByEquipoIdAndJugadorId(equipoId, jugadorId)
                .orElseThrow(()-> new ResourceNotFoundException("El jugador no pertenece a este equipo"));

        equipoJugador.setActivo(false);
        equipoJugador.setFechaBaja(LocalDateTime.now());
        equipoJugadorRepository.save(equipoJugador);
        logService.registrar("EquipoJugador", jugadorId, LogModificacion.AccionLog.EDITAR, null, null, null);
    }

    /**
     * Devuelve la lista de jugadores de un equipo en formato simple.
     *
     * @param equipoId identificador del equipo
     * @return lista de {@link JugadorSimpleDTO} pertenecientes al equipo
     * @throws ResourceNotFoundException si no existe ningún equipo con ese id
     */
    @Transactional(readOnly = true)
    public List<JugadorSimpleDTO> obtenerJugadoresSimple(long equipoId){
        if (!equipoRepository.existsById(equipoId)){
            throw new ResourceNotFoundException("Equipo","id", equipoId);
        }

        return jugadorRepository.findByEquipoId(equipoId)
                .stream()
                .map(jugadorMapper::toSimpleDTO)
                .toList();
    }

    /**
     * Devuelve la lista de jugadores de un equipo en formato detalle.
     *
     * @param equipoId identificador del equipo
     * @return lista de {@link JugadorDetalleDTO} pertenecientes al equipo
     * @throws ResourceNotFoundException si no existe ningún equipo con ese id
     */
    @Transactional(readOnly = true)
    public List<JugadorDetalleDTO> obtenerJugadoresDetalle(long equipoId){
        if (!equipoRepository.existsById(equipoId)){
            throw new ResourceNotFoundException("Equipo","id", equipoId);
        }

        return jugadorRepository.findByEquipoId(equipoId)
                .stream()
                .map(jugadorMapper::toDetalleDTO)
                .toList();
    }

    /**
     * Asigna un usuario como manager de un equipo en una competición concreta.
     *
     * <p>La relación manager es específica de la competición: el mismo usuario puede
     * ser manager del mismo equipo en distintas competiciones.</p>
     *
     * @param equipoId     identificador del equipo
     * @param competicionId identificador de la competición
     * @param usuarioId    identificador del usuario a asignar como manager
     * @throws ResourceNotFoundException si el equipo o el usuario no existen
     * @throws BadRequestException       si el usuario ya es manager del equipo en esa competición
     */
    @Transactional
    public void asignarManager(long equipoId, long competicionId, long usuarioId) {
        if (!equipoRepository.existsById(equipoId)) {
            throw new ResourceNotFoundException("Equipo", "id", usuarioId);
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(()-> new ResourceNotFoundException("Usuario", "id", usuarioId));

        if (equipoManagerRepository.existsByEquipoIdAndCompeticionIdAndUsuarioId(equipoId, competicionId, usuarioId)) {
            throw new BadRequestException("El usuario ya es manager de este equipo en esta competición");
        }

        Equipo equipo = equipoRepository.getReferenceById(equipoId);
        Competicion competicion = new Competicion();
        competicion.setId(competicionId);

        EquipoManager manager = EquipoManager.builder()
                .equipo(equipo)
                .competicion(competicion)
                .usuario(usuario)
                .build();

        equipoManagerRepository.save(manager);
    }
}
