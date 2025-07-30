package com.example.modelo;

import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un Ciclo de Cine en el sistema de gestión municipal.
 * 
 * Un Ciclo de Cine es un tipo específico de evento cultural que consiste en
 * la proyección de múltiples películas organizadas temáticamente. Puede incluir
 * charlas posteriores y debates sobre las obras cinematográficas presentadas.
 * 
 * Esta clase extiende de {@link Evento}, heredando todas las propiedades básicas
 * como nombre, fecha de inicio, duración, estado y responsables, y añade
 * características específicas del ciclo cinematográfico.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 * @see Pelicula
 */
@Entity
@Table(name="CicloDeCine")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CicloDeCine extends Evento {
    
    /**
     * Indica si el ciclo de cine incluye charlas posteriores a las proyecciones.
     * 
     * Las charlas pueden incluir:
     * - Debates sobre las temáticas de las películas
     * - Análisis cinematográfico con especialistas
     * - Intercambio de opiniones con el público
     * - Presentaciones de directores o críticos
     * 
     * @implNote Por defecto es false si no se especifica
     */
    private boolean hayCharlas;

    /**
     * Lista de películas que componen el ciclo de cine.
     * 
     * Cada película mantiene una referencia al ciclo al que pertenece mediante
     * el campo 'cicloDeCine'. La relación es bidireccional y se maneja con
     * cascade ALL, lo que significa que:
     * - Al persistir un ciclo, se persisten automáticamente sus películas
     * - Al eliminar un ciclo, se eliminan automáticamente sus películas
     * - Al actualizar un ciclo, se actualizan automáticamente sus películas
     * 
     * Las películas deben estar ordenadas según el campo 'orden' en la entidad Película.
     * 
     * @see Pelicula#getCicloDeCine()
     * @see Pelicula#getOrden()
     */
    @OneToMany(mappedBy = "cicloDeCine", cascade = CascadeType.ALL)
    private List<Pelicula> peliculas;
    
    /**
     * Obtiene la cantidad total de películas en el ciclo.
     * 
     * @return número de películas en el ciclo, 0 si la lista es null o está vacía
     */
    public int getCantidadPeliculas() {
        return peliculas != null ? peliculas.size() : 0;
    }
    
    /**
     * Verifica si el ciclo tiene películas asignadas.
     * 
     * @return true si tiene al menos una película, false en caso contrario
     */
    public boolean tienePeliculas() {
        return peliculas != null && !peliculas.isEmpty();
    }
    
    /**
     * Representación textual del ciclo de cine.
     * 
     * @return string descriptivo incluyendo el nombre del evento, cantidad de películas
     *         y si incluye charlas
     */
    @Override
    public String toString() {
        return String.format("CicloDeCine{nombre='%s', peliculas=%d, hayCharlas=%s}", 
                           getNombre(), getCantidadPeliculas(), hayCharlas);
    }
}