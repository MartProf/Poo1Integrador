package com.example.modelo;

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
 * Entidad que representa una película dentro de un ciclo de cine municipal.
 * 
 * Esta clase almacena la información de cada película que forma parte de
 * un {@link CicloDeCine}. Las películas se organizan en un orden específico
 * de proyección dentro del ciclo, permitiendo una programación secuencial
 * de las obras cinematográficas que serán exhibidas durante el evento.
 * 
 * La relación Many-to-One con CicloDeCine permite que múltiples películas
 * pertenezcan a un mismo ciclo, facilitando la gestión de programación
 * cinematográfica municipal.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see CicloDeCine
 */
@Entity
@Table(name="Pelicula")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pelicula {
    
    /**
     * Identificador único de la película en la base de datos.
     * 
     * Se genera automáticamente utilizando la estrategia IDENTITY,
     * que delega la generación del ID al motor de base de datos.
     * Cada película tiene su propio identificador único dentro del sistema.
     * 
     * @implNote Este campo es inmutable una vez asignado por la base de datos
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título de la película.
     * 
     * Nombre oficial de la obra cinematográfica que será proyectada.
     * Debe ser claro y reconocible para que los asistentes puedan
     * identificar fácilmente la película en la programación del ciclo.
     * 
     * @implNote No puede ser null ni vacío, debe contener el título
     *           oficial de la película
     */
    private String titulo;
    
    /**
     * Orden de proyección de la película dentro del ciclo de cine.
     * 
     * Número que indica la posición secuencial en que esta película
     * será proyectada durante el ciclo. Permite organizar la programación
     * cronológica de las obras y facilita la planificación de horarios.
     * 
     * El sistema valida que no existan dos películas con el mismo orden
     * dentro del mismo ciclo para evitar conflictos de programación.
     * 
     * @implNote Debe ser un número positivo y único dentro del ciclo.
     *           El orden 1 corresponde a la primera película del ciclo
     */
    private int orden;

    /**
     * Ciclo de cine al cual pertenece esta película.
     * 
     * Esta relación Many-to-One establece a qué evento específico de
     * ciclo de cine pertenece la película. Permite que un ciclo tenga
     * múltiples películas organizadas en secuencia.
     * 
     * La relación es obligatoria ya que toda película debe estar
     * asociada a un ciclo específico para su proyección.
     * 
     * @implNote No puede ser null, toda película debe pertenecer
     *           a un ciclo de cine válido
     * 
     * @see CicloDeCine
     */
    @ManyToOne
    @JoinColumn(name = "cicloDeCine_id")
    private CicloDeCine cicloDeCine;
}
