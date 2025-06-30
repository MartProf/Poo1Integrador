package com.example.modelo;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "concierto")
public class Concierto extends Evento {
    @ManyToMany
    @JoinTable(
        name = "concierto_artistas",
        joinColumns = @JoinColumn(name = "concierto_id"),
        inverseJoinColumns = @JoinColumn(name = "persona_id")
    )
    private List<Persona> artistas;
    
    private boolean entradaGratuita;
    private String lugar;
    
    // Getters y setters
}
    