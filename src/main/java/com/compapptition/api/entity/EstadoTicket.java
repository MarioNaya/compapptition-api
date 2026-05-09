package com.compapptition.api.entity;

/**
 * Estados posibles de un ticket de soporte.
 *
 * <p>Flujo típico: un usuario crea un ticket en estado {@link #ABIERTO}.
 * El admin de sistema lo pasa a {@link #EN_PROCESO} cuando empieza a
 * atenderlo, y a {@link #RESUELTO} cuando ha respondido o aplicado un
 * fix. {@link #CERRADO} se usa para tickets descartados (duplicados,
 * spam, fuera de alcance) o tickets ya resueltos sobre los que se
 * confirma cierre.</p>
 *
 * <p>Cada cambio de estado por parte del admin emite una notificación
 * in-app al autor del ticket vía {@code NotificacionService}.</p>
 *
 * @author Mario
 */
public enum EstadoTicket {
    ABIERTO,
    EN_PROCESO,
    RESUELTO,
    CERRADO
}
