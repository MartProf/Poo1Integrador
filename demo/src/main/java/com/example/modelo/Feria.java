package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una Feria en el sistema de gestión municipal.
 * 
 * Una Feria es un tipo específico de evento cultural y comercial que consiste
 * en la exhibición y venta de productos, servicios o manifestaciones culturales
 * organizados en stands o puestos. Las ferias pueden ser gastronómicas, artesanales,
 * comerciales o temáticas, y forman parte importante de la actividad económica
 * y cultural del municipio.
 * 
 * Esta clase extiende de {@link Evento}, heredando todas las propiedades básicas
 * como nombre, fecha de inicio, duración, estado y responsables, y añade
 * características específicas de la feria como la cantidad de stands disponibles
 * y si se realiza al aire libre o en espacios cerrados.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 */
@Entity
@Table(name="Feria")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Feria extends Evento {
    
    /**
     * Número total de stands o puestos disponibles en la feria.
     * 
     * Representa la capacidad máxima de expositores que pueden participar
     * en la feria. Este número determina:
     * - El tamaño y alcance del evento
     * - La planificación logística del espacio
     * - Los ingresos potenciales por alquileres de stands
     * - La cantidad máxima de inscripciones de expositores
     * 
     * @implNote Debe ser un valor positivo mayor a 0, ya que una feria
     *           sin stands no tendría sentido organizativo
     */
    private int cantidadDeStand;
    
    /**
     * Indica si la feria se realiza al aire libre o en espacios cerrados.
     * 
     * Valores posibles:
     * - true: Feria al aire libre (plazas, parques, calles)
     * - false: Feria en espacios cerrados (centros de convenciones, gimnasios)
     * 
     * Esta información es crucial para:
     * - La planificación logística (protección climática, electricidad)
     * - La comunicación al público (vestimenta adecuada)
     * - La gestión de permisos municipales
     * - Las medidas de contingencia por clima adverso
     * 
     * @implNote Las ferias al aire libre requieren consideraciones especiales
     *           para condiciones meteorológicas adversas
     */
    private boolean alAirelibre;
}