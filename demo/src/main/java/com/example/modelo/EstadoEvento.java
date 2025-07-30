package com.example.modelo;

/**
 * Enumeración que define los estados posibles de un evento en el sistema municipal.
 * 
 * Esta enumeración representa el ciclo de vida completo de un evento, desde su
 * planificación inicial hasta su finalización. Cada estado determina las acciones
 * disponibles y la visibilidad del evento para los participantes.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 */
public enum EstadoEvento {

    /**
     * Estado inicial de un evento cuando está siendo planificado.
     * 
     * En este estado:
     * - El evento está en proceso de organización
     * - Los detalles pueden estar incompletos
     * - No está disponible para inscripciones del público
     * - Solo visible para organizadores y responsables
     */
    PLANIFICADO,
    
    /**
     * Estado cuando el evento ha sido confirmado y está listo para publicación.
     * 
     * En este estado:
     * - Todos los detalles del evento están definidos
     * - El evento es visible para el público
     * - Las inscripciones están abiertas (si aplica)
     * - Aparece en la lista de eventos disponibles
     */
    CONFIRMADO,
    
    /**
     * Estado cuando el evento está actualmente en desarrollo.
     * 
     * En este estado:
     * - El evento está ocurriendo en tiempo real
     * - Puede seguir recibiendo inscripciones de último momento
     * - Es visible para el público como evento en curso
     * - Los responsables pueden realizar ajustes operativos
     */
    EN_EJECUCION,
    
    /**
     * Estado final cuando el evento ha concluido.
     * 
     * En este estado:
     * - El evento ha terminado completamente
     * - No acepta más inscripciones
     * - No aparece en eventos disponibles
     * - Queda registrado para fines históricos y estadísticos
     */
    FINALIZADO
}
