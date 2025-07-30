package com.example.modelo;

import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un Concierto en el sistema de gestión municipal.
 * 
 * Un Concierto es un tipo específico de evento cultural que involucra la
 * presentación musical de uno o varios artistas ante una audiencia. Puede ser
 * gratuito o pago, y forma parte de la programación cultural del municipio.
 * 
 * Esta clase extiende de {@link Evento}, heredando todas las propiedades básicas
 * como nombre, fecha de inicio, duración, estado y responsables, y añade
 * características específicas del concierto como artistas participantes y
 * política de entrada.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 * @see Persona
 */
@Entity
@Table(name="Concierto")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Concierto extends Evento {

    /**
     * Lista de artistas que participarán en el concierto.
     * 
     * Esta relación Many-to-Many permite que:
     * - Un concierto tenga múltiples artistas (banda, varios solistas, etc.)
     * - Un artista pueda participar en múltiples conciertos
     * 
     * La tabla intermedia 'concierto_artistas' almacena las relaciones entre
     * conciertos y personas (artistas). Esta tabla se crea automáticamente
     * con las claves foráneas correspondientes.
     * 
     * @implNote Los artistas deben ser instancias de {@link Persona} que
     *           representen músicos, cantantes, DJ's u otros performers
     * 
     * @see Persona
     */
    @ManyToMany
    @JoinTable(
        name = "concierto_artistas",
        joinColumns = @JoinColumn(name = "concierto_id"), 
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> artistas;
    
    /**
     * Indica si la entrada al concierto es gratuita o requiere pago.
     * 
     * Valores posibles:
     * - true: Entrada gratuita, sin costo para el público
     * - false: Entrada paga, requiere adquisición de tickets
     * 
     * Esta información es crucial para la gestión logística del evento y
     * para informar correctamente al público sobre las condiciones de acceso.
     * 
     * @implNote Por defecto es false (entrada paga) si no se especifica
     */
    private boolean entradaGratuita;
}
    