package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un Taller en el sistema de gestión municipal.
 * 
 * Un Taller es un tipo específico de evento educativo y formativo que
 * proporciona capacitación práctica en diversas disciplinas. Se caracteriza
 * por tener un cupo limitado de participantes, un instructor especializado
 * y puede realizarse en modalidad presencial o virtual.
 * 
 * Esta clase extiende de {@link Evento}, heredando todas las propiedades básicas
 * como nombre, fecha de inicio, duración, estado y responsables, e implementa
 * la interfaz {@link TieneCupo} para gestionar la capacidad de participantes.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 * @see TieneCupo
 * @see Persona
 * @see Modalidad
 */
@Entity
@Table(name="Taller")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Taller extends Evento implements TieneCupo{
    
    /**
     * Número máximo de participantes permitidos en el taller.
     * 
     * Define la capacidad total del taller, limitando las inscripciones
     * para garantizar la calidad educativa y permitir una atención
     * personalizada por parte del instructor. Una vez alcanzado este
     * número, el taller no acepta más inscripciones.
     * 
     * @implNote Debe ser un valor positivo mayor a 0, ya que un taller
     *           sin cupo no tendría utilidad práctica
     */
    private int cupoMaximo;

    /**
     * Persona responsable de dictar e instruir el taller.
     * 
     * El instructor es el especialista encargado de:
     * - Planificar y estructurar el contenido del taller
     * - Dirigir las sesiones de aprendizaje
     * - Proporcionar orientación y retroalimentación a los participantes
     * - Evaluar el progreso y logros de los asistentes
     * 
     * Esta relación Many-to-One permite que:
     * - Un taller tenga un instructor principal
     * - Un instructor pueda dictar múltiples talleres
     * 
     * @implNote El instructor debe ser una {@link Persona} con conocimientos
     *           y experiencia en la temática del taller
     * 
     * @see Persona
     */
    @ManyToOne
    @JoinColumn(name = "instructor_id", unique = false)
    private Persona instructor;

    /**
     * Modalidad en que se realiza el taller (presencial o virtual).
     * 
     * Determina la forma de ejecución del taller, lo cual afecta:
     * - Los recursos tecnológicos necesarios
     * - La planificación logística del espacio
     * - Las estrategias pedagógicas a emplear
     * - Los requisitos de participación para los asistentes
     * 
     * @see Modalidad
     */
    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;
    
    /**
     * Obtiene el cupo máximo de participantes del taller.
     * 
     * Implementación del método requerido por la interfaz {@link TieneCupo}.
     * 
     * @return el número máximo de participantes permitidos
     */
    @Override
    public int getCupoMaximo() {
        return cupoMaximo;
    }

    /**
     * Calcula el cupo disponible actual del taller.
     * 
     * Implementación del método requerido por la interfaz {@link TieneCupo}.
     * Calcula la diferencia entre el cupo máximo y el número actual de
     * participantes inscritos.
     * 
     * @return el número de plazas disponibles para nuevas inscripciones.
     *         Retorna 0 si el taller está completo
     */
    @Override
    public int getCupoDisponible() {
        // Retorna cupo máximo menos participantes inscritos
        int inscritos = (getParticipantes() != null) ? getParticipantes().size() : 0;
        return cupoMaximo - inscritos;  
    }

    
}
