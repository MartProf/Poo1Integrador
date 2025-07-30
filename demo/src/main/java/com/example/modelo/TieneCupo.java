package com.example.modelo;

/**
 * Interfaz que define el comportamiento para eventos que manejan capacidad limitada.
 * 
 * Esta interfaz establece el contrato para aquellos tipos de eventos que tienen
 * un número máximo de participantes permitidos y necesitan gestionar la
 * disponibilidad de cupos. Proporciona métodos estándar para consultar tanto
 * la capacidad total como el cupo disponible en tiempo real.
 * 
 * La implementación de esta interfaz permite un manejo uniforme de la capacidad
 * en diferentes tipos de eventos, facilitando validaciones de inscripción y
 * la presentación de información de disponibilidad al usuario.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Taller
 */
public interface TieneCupo {

    /**
     * Obtiene la capacidad máxima de participantes para el evento.
     * 
     * Define el límite superior de inscripciones que puede recibir el evento.
     * Este valor es establecido durante la planificación del evento y
     * determina cuándo se debe cerrar el proceso de inscripciones.
     * 
     * @return el número máximo de participantes permitidos, siempre mayor a 0
     */
    public int getCupoMaximo();
    
    /**
     * Calcula el número de cupos disponibles para nuevas inscripciones.
     * 
     * Proporciona la cantidad actual de plazas libres, calculada como la
     * diferencia entre el cupo máximo y el número de participantes ya
     * inscritos. Un valor de 0 indica que el evento está completo.
     * 
     * @return el número de cupos disponibles para inscripción.
     *         Retorna 0 si el evento ha alcanzado su capacidad máxima
     */
    public int getCupoDisponible();
}
