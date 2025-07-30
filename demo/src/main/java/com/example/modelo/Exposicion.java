package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una Exposición en el sistema de gestión municipal.
 * 
 * Una Exposición es un tipo específico de evento cultural que consiste en la
 * exhibición pública de obras de arte, objetos históricos, fotografías u otros
 * elementos culturales organizados temáticamente. Forma parte de la programación
 * cultural del municipio y está dirigida por un curador especializado.
 * 
 * Esta clase extiende de {@link Evento}, heredando todas las propiedades básicas
 * como nombre, fecha de inicio, duración, estado y responsables, y añade
 * características específicas de la exposición como el tipo de arte y el curador
 * a cargo de la curaduría.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 * @see Persona
 */
@Entity
@Table(name="Exposicion")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Exposicion extends Evento {
    
    /**
     * Tipo o categoría de arte que se exhibe en la exposición.
     * 
     * Define la temática o disciplina artística principal de la exposición.
     * Ejemplos comunes incluyen:
     * - "Pintura contemporánea"
     * - "Fotografía histórica"
     * - "Escultura moderna"
     * - "Arte digital"
     * - "Patrimonio arqueológico"
     * 
     * Esta información ayuda a los visitantes a identificar el contenido
     * y decidir su interés en la exposición.
     * 
     * @implNote Debe ser descriptivo y específico del contenido expuesto
     */
    private String tipoArte;

    /**
     * Persona responsable de la curaduría de la exposición.
     * 
     * El curador es el especialista encargado de:
     * - Seleccionar las obras o elementos a exhibir
     * - Organizar la distribución espacial de la exposición
     * - Elaborar el concepto temático y narrativo
     * - Supervisar el montaje y desmontaje
     * - Proporcionar contexto educativo sobre las obras
     * 
     * Esta relación Many-to-One permite que:
     * - Una exposición tenga un curador principal
     * - Un curador pueda estar a cargo de múltiples exposiciones
     * 
     * @implNote El curador debe ser una {@link Persona} con experiencia
     *           en el tipo de arte de la exposición
     * 
     * @see Persona
     */
    @ManyToOne
    @JoinColumn(name = "curador_id", unique = false)
    private Persona curador;

    
}
