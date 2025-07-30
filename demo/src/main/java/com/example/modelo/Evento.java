package com.example.modelo;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.InheritanceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase abstracta base que representa un evento en el sistema de gestión municipal.
 * 
 * Esta clase define las propiedades y comportamientos comunes a todos los tipos
 * de eventos culturales del municipio (conciertos, talleres, exposiciones, etc.).
 * Utiliza el patrón de herencia JOINED de JPA para permitir que las subclases
 * tengan sus propias tablas mientras comparten una tabla base común.
 * 
 * Cada evento tiene un ciclo de vida definido por su estado, desde la planificación
 * hasta su finalización, y puede tener múltiples responsables y participantes.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see EstadoEvento
 * @see Persona
 * @see Participante
 * @see Concierto
 * @see CicloDeCine
 * @see Taller
 * @see Exposicion
 * @see Feria
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="Evento")
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public abstract class Evento {
    
    /**
     * Identificador único del evento en la base de datos.
     * 
     * Se genera automáticamente utilizando la estrategia IDENTITY,
     * que delega la generación del ID al motor de base de datos.
     * 
     * @implNote Este campo es inmutable una vez asignado por la base de datos
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Nombre descriptivo del evento.
     * 
     * Debe ser claro y representativo del contenido del evento para que
     * los ciudadanos puedan identificarlo fácilmente en las listas y
     * búsquedas del sistema.
     * 
     * @implNote No puede ser null y debe ser único por tipo de evento
     */
    private String nombre;
    
    /**
     * Fecha de inicio del evento.
     * 
     * Representa el primer día en que el evento tendrá lugar. Se utiliza
     * para ordenar eventos cronológicamente y determinar la disponibilidad
     * temporal del evento.
     * 
     * @implNote Debe ser una fecha futura al momento de creación del evento
     */
    private LocalDate fechaInicio;
    
    /**
     * Duración del evento expresada en días.
     * 
     * Indica cuántos días consecutivos durará el evento, comenzando desde
     * la fecha de inicio. Un valor de 1 indica que el evento dura un solo día.
     * 
     * @implNote Debe ser un valor positivo mayor a 0
     */
    private int duraciónDias;

    /**
     * Estado actual del evento en su ciclo de vida.
     * 
     * Determina la fase en que se encuentra el evento y controla su visibilidad
     * y disponibilidad para los participantes. Los eventos solo aparecen en
     * la lista pública cuando están CONFIRMADO o EN_EJECUCION.
     * 
     * @see EstadoEvento
     */
    @Enumerated(EnumType.STRING)
    private EstadoEvento estado;

    /**
     * Lista de personas responsables de la organización y gestión del evento.
     * 
     * Esta relación Many-to-Many permite que:
     * - Un evento tenga múltiples responsables (coordinación compartida)
     * - Una persona pueda ser responsable de múltiples eventos
     * 
     * Los responsables tienen permisos especiales para modificar el evento
     * y gestionar sus participantes. La tabla intermedia 'evento_responsables'
     * almacena estas relaciones.
     * 
     * @implNote Todo evento debe tener al menos un responsable asignado
     * @see Persona
     */
    @ManyToMany
    @JoinTable(
        name = "evento_responsables",
        joinColumns = @JoinColumn(name = "evento_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> responsables;

    /**
     * Lista de participantes inscritos en el evento.
     * 
     * Esta relación One-to-Many representa todas las inscripciones realizadas
     * para este evento específico. Cada participante contiene información sobre
     * la persona inscrita y los detalles de su participación.
     * 
     * La configuración cascade=ALL y orphanRemoval=true garantiza que:
     * - Al eliminar un evento se eliminan todas sus inscripciones
     * - Al remover un participante de la lista se elimina de la base de datos
     * 
     * @see Participante
     */
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participante> participantes;
}
    