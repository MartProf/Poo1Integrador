package com.example.modelo;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa la participación de una persona en un evento específico.
 * 
 * Esta clase funciona como tabla de unión entre {@link Persona} y {@link Evento},
 * almacenando información específica sobre cada inscripción. Permite rastrear
 * quién se inscribió en qué evento y cuándo lo hizo, facilitando la gestión
 * de participantes y el control de capacidad de los eventos municipales.
 * 
 * La relación Many-to-One con ambas entidades permite que:
 * - Una persona pueda participar en múltiples eventos
 * - Un evento pueda tener múltiples participantes
 * - Se mantenga un registro histórico de todas las participaciones
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Persona
 * @see Evento
 */
@Entity
@Table(name="Participante")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Participante {
    
    /**
     * Identificador único de la participación en la base de datos.
     * 
     * Se genera automáticamente utilizando la estrategia IDENTITY,
     * que delega la generación del ID al motor de base de datos.
     * Cada registro de participación tiene su propio identificador único.
     * 
     * @implNote Este campo es inmutable una vez asignado por la base de datos
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Persona que se inscribe para participar en el evento.
     * 
     * Esta relación Many-to-One establece qué ciudadano específico
     * se ha registrado para el evento. La persona debe existir previamente
     * en el sistema antes de poder inscribirse a cualquier evento.
     * 
     * @implNote No puede ser null, toda participación debe estar asociada
     *           a una persona válida del sistema
     * 
     * @see Persona
     */
    @ManyToOne
    @JoinColumn(name = "persona_id")
    Persona persona;

    /**
     * Evento en el cual la persona se inscribe para participar.
     * 
     * Esta relación Many-to-One establece a qué evento específico
     * se refiere esta inscripción. El evento debe existir y estar
     * en un estado que permita inscripciones (CONFIRMADO o EN_EJECUCION).
     * 
     * @implNote No puede ser null, toda participación debe estar asociada
     *           a un evento válido del sistema
     * 
     * @see Evento
     */
    @ManyToOne
    @JoinColumn(name = "evento_id")
    Evento evento;

    /**
     * Fecha en que se realizó la inscripción al evento.
     * 
     * Registra el momento exacto en que la persona se inscribió,
     * lo cual es útil para:
     * - Ordenar participantes por orden de llegada
     * - Aplicar políticas de "primero en llegar, primero en ser atendido"
     * - Generar reportes estadísticos de inscripciones
     * - Validar inscripciones dentro de plazos establecidos
     * 
     * @implNote Debe ser una fecha válida, generalmente la fecha actual
     *           al momento de la inscripción
     */
    LocalDate fechaincripción;
    
}
    