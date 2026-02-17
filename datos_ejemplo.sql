-- Script de datos de ejemplo para la base de datos compapp_test
-- Autor: Generado automáticamente
-- Fecha: 2026-02-12

USE compapp_test;

-- Deshabilitar verificación de claves foráneas temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- Limpiar tablas existentes (opcional - comentar si no quieres borrar datos)
TRUNCATE TABLE estadistica_jugador_evento;
TRUNCATE TABLE evento_equipo;
TRUNCATE TABLE evento;
TRUNCATE TABLE clasificacion;
TRUNCATE TABLE usuario_rol_competicion;
TRUNCATE TABLE equipo_manager;
TRUNCATE TABLE invitacion;
TRUNCATE TABLE log_modificacion;
TRUNCATE TABLE equipo_jugador;
TRUNCATE TABLE competicion_equipo;
TRUNCATE TABLE configuracion_competicion;
TRUNCATE TABLE competicion;
TRUNCATE TABLE tipo_estadistica;
TRUNCATE TABLE jugador;
TRUNCATE TABLE equipo;
TRUNCATE TABLE deporte;
TRUNCATE TABLE rol;
TRUNCATE TABLE usuario;

-- ============================================================
-- USUARIOS
-- ============================================================
INSERT INTO usuario (id, username, email, password, nombre, apellidos, activo, fecha_creacion, fecha_actualizacion) VALUES
(1, 'admin', 'admin@compapp.com', '$2a$10$XPTYXhhPz7szYWVJNzKpf.WYJ8iHJzZvJl/pKxKlLbVfqYiU8eO4K', 'Admin', 'Sistema', 1, NOW(), NOW()),
(2, 'mario.naya', 'mario.naya@email.com', '$2a$10$XPTYXhhPz7szYWVJNzKpf.WYJ8iHJzZvJl/pKxKlLbVfqYiU8eO4K', 'Mario', 'Naya De Luis', 1, NOW(), NOW()),
(3, 'juan.perez', 'juan.perez@email.com', '$2a$10$XPTYXhhPz7szYWVJNzKpf.WYJ8iHJzZvJl/pKxKlLbVfqYiU8eO4K', 'Juan', 'Pérez García', 1, NOW(), NOW()),
(4, 'ana.gomez', 'ana.gomez@email.com', '$2a$10$XPTYXhhPz7szYWVJNzKpf.WYJ8iHJzZvJl/pKxKlLbVfqYiU8eO4K', 'Ana', 'Gómez López', 1, NOW(), NOW()),
(5, 'carlos.ruiz', 'carlos.ruiz@email.com', '$2a$10$XPTYXhhPz7szYWVJNzKpf.WYJ8iHJzZvJl/pKxKlLbVfqYiU8eO4K', 'Carlos', 'Ruiz Martínez', 1, NOW(), NOW()),
(6, 'lucia.martin', 'lucia.martin@email.com', '$2a$10$XPTYXhhPz7szYWVJNzKpf.WYJ8iHJzZvJl/pKxKlLbVfqYiU8eO4K', 'Lucía', 'Martín Sánchez', 1, NOW(), NOW());

-- ============================================================
-- ROLES
-- ============================================================
INSERT INTO rol (id, nombre, descripcion) VALUES
(1, 'ADMIN_SISTEMA', 'Administrador del sistema con acceso completo'),
(2, 'ADMIN_COMPETICION', 'Administrador de una competición específica'),
(3, 'MANAGER_EQUIPO', 'Manager de un equipo en una competición'),
(4, 'JUGADOR', 'Jugador participante en competiciones'),
(5, 'ARBITRO', 'Árbitro de eventos deportivos'),
(6, 'INVITADO', 'Usuario invitado con permisos limitados');

-- ============================================================
-- DEPORTES
-- ============================================================
INSERT INTO deporte (id, nombre, descripcion, activo, icono) VALUES
(1, 'Fútbol', 'Fútbol 11 tradicional', 1, NULL),
(2, 'Fútbol Sala', 'Fútbol sala o fútbol 5', 1, NULL),
(3, 'Baloncesto', 'Baloncesto 5 vs 5', 1, NULL),
(4, 'Voleibol', 'Voleibol 6 vs 6', 1, NULL),
(5, 'Pádel', 'Pádel 2 vs 2', 1, NULL);

-- ============================================================
-- TIPOS DE ESTADÍSTICA
-- ============================================================
-- Fútbol
INSERT INTO tipo_estadistica (id, nombre, descripcion, tipo_valor, orden, activo, deporte_id) VALUES
(1, 'Goles', 'Número de goles marcados', 'ENTERO', 1, 1, 1),
(2, 'Asistencias', 'Número de asistencias', 'ENTERO', 2, 1, 1),
(3, 'Tarjetas Amarillas', 'Tarjetas amarillas recibidas', 'ENTERO', 3, 1, 1),
(4, 'Tarjetas Rojas', 'Tarjetas rojas recibidas', 'ENTERO', 4, 1, 1),
(5, 'Minutos Jugados', 'Minutos jugados en el partido', 'ENTERO', 5, 1, 1);

-- Baloncesto
INSERT INTO tipo_estadistica (id, nombre, descripcion, tipo_valor, orden, activo, deporte_id) VALUES
(6, 'Puntos', 'Puntos anotados', 'ENTERO', 1, 1, 3),
(7, 'Rebotes', 'Rebotes capturados', 'ENTERO', 2, 1, 3),
(8, 'Asistencias', 'Asistencias realizadas', 'ENTERO', 3, 1, 3),
(9, 'Robos', 'Robos de balón', 'ENTERO', 4, 1, 3),
(10, 'Tapones', 'Tapones realizados', 'ENTERO', 5, 1, 3);

-- ============================================================
-- EQUIPOS
-- ============================================================
INSERT INTO equipo (id, nombre, descripcion, tipo, escudo_equipo) VALUES
(1, 'Real Madrid CF', 'Equipo histórico de fútbol de Madrid', 'ESTANDAR', NULL),
(2, 'FC Barcelona', 'Club de fútbol de Barcelona', 'ESTANDAR', NULL),
(3, 'Atlético de Madrid', 'Club Atlético de Madrid', 'ESTANDAR', NULL),
(4, 'Valencia CF', 'Club de fútbol de Valencia', 'ESTANDAR', NULL),
(5, 'Los Tigres FS', 'Equipo de fútbol sala', 'GESTIONADO', NULL),
(6, 'Águilas Basket', 'Equipo de baloncesto', 'GESTIONADO', NULL),
(7, 'Leones FC', 'Equipo de fútbol aficionado', 'GESTIONADO', NULL),
(8, 'Titanes United', 'Equipo multideporte', 'GESTIONADO', NULL);

-- ============================================================
-- JUGADORES
-- ============================================================
INSERT INTO jugador (id, nombre, apellidos, dorsal, posicion, fecha_creacion, foto, usuario_id) VALUES
(1, 'Mario', 'Naya De Luis', 10, 'Delantero', NOW(), NULL, 2),
(2, 'Juan', 'Pérez García', 7, 'Centrocampista', NOW(), NULL, 3),
(3, 'Ana', 'Gómez López', 9, 'Delantera', NOW(), NULL, 4),
(4, 'Carlos', 'Ruiz Martínez', 5, 'Defensa', NOW(), NULL, 5),
(5, 'Lucía', 'Martín Sánchez', 1, 'Portera', NOW(), NULL, 6),
(6, 'Pedro', 'López Fernández', 11, 'Extremo', NOW(), NULL, NULL),
(7, 'María', 'González Ruiz', 8, 'Centrocampista', NOW(), NULL, NULL),
(8, 'David', 'Sánchez Torres', 3, 'Defensa', NOW(), NULL, NULL),
(9, 'Laura', 'Jiménez Molina', 15, 'Delantera', NOW(), NULL, NULL),
(10, 'Miguel', 'Fernández Cruz', 4, 'Defensa', NOW(), NULL, NULL);

-- ============================================================
-- EQUIPO-JUGADOR (Relación)
-- ============================================================
INSERT INTO equipo_jugador (id, equipo_id, jugador_id, dorsal_equipo, activo, fecha_alta, fecha_baja) VALUES
(1, 1, 1, 10, 1, NOW(), NULL),
(2, 1, 2, 7, 1, NOW(), NULL),
(3, 1, 5, 1, 1, NOW(), NULL),
(4, 2, 3, 9, 1, NOW(), NULL),
(5, 2, 4, 5, 1, NOW(), NULL),
(6, 3, 6, 11, 1, NOW(), NULL),
(7, 3, 7, 8, 1, NOW(), NULL),
(8, 4, 8, 3, 1, NOW(), NULL),
(9, 4, 9, 15, 1, NOW(), NULL),
(10, 4, 10, 4, 1, NOW(), NULL);

-- ============================================================
-- COMPETICIONES
-- ============================================================
INSERT INTO competicion (id, nombre, descripcion, deporte_id, creador_id, estado, fecha_creacion, fecha_inicio, fecha_fin, fecha_actualizacion, publica, inscripcion_abierta, estadisticas_activas) VALUES
(1, 'Liga Nacional 2025-2026', 'Liga nacional de fútbol temporada 2025-2026', 1, 1, 'ACTIVA', '2025-08-01', '2025-09-01', '2026-05-31', NOW(), 1, 0, 1),
(2, 'Copa de Primavera 2026', 'Torneo eliminatorio de fútbol', 1, 2, 'ACTIVA', '2026-01-15', '2026-02-01', '2026-04-30', NOW(), 1, 1, 1),
(3, 'Torneo Baloncesto Verano', 'Torneo de baloncesto de verano', 3, 1, 'BORRADOR', '2026-02-10', '2026-06-01', '2026-08-31', NOW(), 1, 1, 0),
(4, 'Liga Fútbol Sala Invierno', 'Liga de fútbol sala', 2, 2, 'ACTIVA', '2025-11-01', '2025-12-01', '2026-03-31', NOW(), 0, 0, 1);

-- ============================================================
-- CONFIGURACIÓN DE COMPETICIÓN
-- ============================================================
INSERT INTO configuracion_competicion (id, competicion_id, formato, puntos_victoria, puntos_empate, puntos_derrota, num_equipos_playoff, partidos_eliminatoria) VALUES
(1, 1, 'LIGA_IDA_VUELTA', 3, 1, 0, NULL, NULL),
(2, 2, 'PLAYOFF', 3, 1, 0, 8, 1),
(3, 3, 'LIGA_PLAYOFF', 2, 1, 0, 4, 2),
(4, 4, 'LIGA', 3, 1, 0, NULL, NULL);

-- ============================================================
-- COMPETICIÓN-EQUIPO (Inscripciones)
-- ============================================================
INSERT INTO competicion_equipo (id, competicion_id, equipo_id, activo, fecha_inscripcion) VALUES
(1, 1, 1, 1, '2025-08-15 10:00:00'),
(2, 1, 2, 1, '2025-08-15 11:30:00'),
(3, 1, 3, 1, '2025-08-16 09:00:00'),
(4, 1, 4, 1, '2025-08-16 14:00:00'),
(5, 2, 1, 1, '2026-01-20 10:00:00'),
(6, 2, 2, 1, '2026-01-20 11:00:00'),
(7, 2, 7, 1, '2026-01-21 10:00:00'),
(8, 2, 8, 1, '2026-01-21 15:00:00');

-- ============================================================
-- CLASIFICACIÓN
-- ============================================================
INSERT INTO clasificacion (id, competicion_id, equipo_id, posicion, partidos_jugados, victorias, empates, derrotas, goles_favor, goles_contra, diferencia_goles, puntos, fecha_actualizacion) VALUES
(1, 1, 1, 1, 20, 15, 3, 2, 48, 18, 30, 48, NOW()),
(2, 1, 2, 2, 20, 14, 4, 2, 52, 22, 30, 46, NOW()),
(3, 1, 3, 3, 20, 12, 5, 3, 40, 25, 15, 41, NOW()),
(4, 1, 4, 4, 20, 8, 6, 6, 32, 28, 4, 30, NOW());

-- ============================================================
-- EVENTOS (Partidos)
-- ============================================================
INSERT INTO evento (id, competicion_id, fecha_evento, fecha_hora, jornada, lugar, estado, resultado_local, resultado_visitante, observaciones, fecha_creacion, fecha_actualizacion) VALUES
(1, 1, '2025-09-05', '2025-09-05 18:00:00', 1, 'Estadio Santiago Bernabéu', 'FINALIZADO', 3, '2', 'Buen partido', NOW(), NOW()),
(2, 1, '2025-09-05', '2025-09-05 20:00:00', 1, 'Estadio Camp Nou', 'FINALIZADO', 2, '1', NULL, NOW(), NOW()),
(3, 1, '2025-09-12', '2025-09-12 18:00:00', 2, 'Estadio Wanda Metropolitano', 'FINALIZADO', 1, '1', NULL, NOW(), NOW()),
(4, 1, '2026-02-15', '2026-02-15 18:00:00', 21, 'Estadio Mestalla', 'PROGRAMADO', NULL, NULL, 'Próximo partido', NOW(), NOW()),
(5, 2, '2026-02-05', '2026-02-05 19:00:00', 1, 'Campo Municipal', 'FINALIZADO', 2, '1', 'Octavos de final', NOW(), NOW());

-- ============================================================
-- EVENTO-EQUIPO (Participantes del evento)
-- ============================================================
INSERT INTO evento_equipo (id, evento_id, equipo_id, es_local) VALUES
(1, 1, 1, 1),
(2, 1, 2, 0),
(3, 2, 3, 1),
(4, 2, 4, 0),
(5, 3, 1, 1),
(6, 3, 3, 0),
(7, 4, 2, 1),
(8, 4, 4, 0),
(9, 5, 1, 1),
(10, 5, 7, 0);

-- ============================================================
-- ESTADÍSTICAS JUGADOR-EVENTO
-- ============================================================
-- Evento 1 (Real Madrid 3-2 Barcelona)
INSERT INTO estadistica_jugador_evento (id, evento_id, jugador_id, tipo_estadistica_id, valor) VALUES
(1, 1, 1, 1, 2.00), -- Mario: 2 goles
(2, 1, 1, 5, 90.00), -- Mario: 90 minutos
(3, 1, 2, 1, 1.00), -- Juan: 1 gol
(4, 1, 2, 2, 1.00), -- Juan: 1 asistencia
(5, 1, 2, 5, 90.00), -- Juan: 90 minutos
(6, 1, 5, 5, 90.00), -- Lucía: 90 minutos (portera)
(7, 1, 3, 1, 2.00), -- Ana (Barça): 2 goles
(8, 1, 4, 3, 1.00); -- Carlos: 1 tarjeta amarilla

-- Evento 2 (Atlético 2-1 Valencia)
INSERT INTO estadistica_jugador_evento (id, evento_id, jugador_id, tipo_estadistica_id, valor) VALUES
(9, 2, 6, 1, 1.00), -- Pedro: 1 gol
(10, 2, 7, 1, 1.00), -- María: 1 gol
(11, 2, 7, 2, 1.00), -- María: 1 asistencia
(12, 2, 9, 1, 1.00); -- Laura (Valencia): 1 gol

-- ============================================================
-- USUARIO-ROL-COMPETICIÓN
-- ============================================================
INSERT INTO usuario_rol_competicion (id, usuario_id, competicion_id, rol_id, fecha_asignacion) VALUES
(1, 1, 1, 1, NOW()), -- Admin es ADMIN_SISTEMA en Liga Nacional
(2, 1, 2, 2, NOW()), -- Admin es ADMIN_COMPETICION en Copa Primavera
(3, 2, 1, 3, NOW()), -- Mario es MANAGER_EQUIPO en Liga Nacional
(4, 3, 2, 4, NOW()), -- Juan es JUGADOR en Copa Primavera
(5, 4, 1, 4, NOW()); -- Ana es JUGADOR en Liga Nacional

-- ============================================================
-- EQUIPO-MANAGER
-- ============================================================
INSERT INTO equipo_manager (id, equipo_id, competicion_id, usuario_id, fecha_asignacion) VALUES
(1, 1, 1, 2, NOW()), -- Mario gestiona Real Madrid en Liga Nacional
(2, 2, 1, 3, NOW()), -- Juan gestiona Barcelona en Liga Nacional
(3, 7, 2, 4, NOW()); -- Ana gestiona Leones FC en Copa Primavera

-- ============================================================
-- INVITACIONES
-- ============================================================
INSERT INTO invitacion (id, emisor_id, destinatario_id, destinatario_email, competicion_id, equipo_id, rol_ofrecido, estado, token, fecha_creacion, fecha_expiracion) VALUES
(1, 1, NULL, 'nuevo.jugador@email.com', 1, 1, 'JUGADOR', 'PENDIENTE', 'abc123def456ghi789', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY)),
(2, 2, 5, 'carlos.ruiz@email.com', 2, 7, 'MANAGER_EQUIPO', 'ACEPTADA', 'xyz789uvw456rst123', NOW() - INTERVAL 5 DAY, DATE_ADD(NOW(), INTERVAL 2 DAY)),
(3, 1, NULL, 'arbitro@email.com', 1, NULL, 'ARBITRO', 'PENDIENTE', 'qwe456rty789uio012', NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY));

-- ============================================================
-- LOG DE MODIFICACIONES
-- ============================================================
INSERT INTO log_modificacion (id, usuario_id, entidad, entidad_id, accion, datos_anteriores, datos_nuevos, competicion_id, fecha, ip_address) VALUES
(1, 1, 'competicion', 1, 'CREAR', NULL, '{"nombre":"Liga Nacional 2025-2026","estado":"BORRADOR"}', 1, NOW() - INTERVAL 6 MONTH, '192.168.1.100'),
(2, 1, 'competicion', 1, 'EDITAR', '{"estado":"BORRADOR"}', '{"estado":"ACTIVA"}', 1, NOW() - INTERVAL 5 MONTH, '192.168.1.100'),
(3, 2, 'evento', 1, 'CREAR', NULL, '{"fecha":"2025-09-05","estado":"PROGRAMADO"}', 1, NOW() - INTERVAL 4 MONTH, '192.168.1.105'),
(4, 2, 'evento', 1, 'EDITAR', '{"estado":"PROGRAMADO"}', '{"estado":"FINALIZADO","resultado":"3-2"}', 1, NOW() - INTERVAL 4 MONTH, '192.168.1.105');

-- Habilitar verificación de claves foráneas nuevamente
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- CONSULTAS DE VERIFICACIÓN (Comentadas - descomentar para usar)
-- ============================================================
-- SELECT * FROM usuario;
-- SELECT * FROM deporte;
-- SELECT * FROM equipo;
-- SELECT * FROM jugador;
-- SELECT * FROM competicion;
-- SELECT * FROM clasificacion ORDER BY posicion;
-- SELECT * FROM evento;
-- SELECT * FROM estadistica_jugador_evento;

-- Mensaje de finalización
SELECT 'Script de datos de ejemplo ejecutado correctamente' AS Resultado;
