package com.example.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "participante")
public class Participante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "persona_id")
    private Persona persona;
    
    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;
    
    private LocalDate fechaInscripcion;
    
    // Getters y setters
    public Long getId() { return id; }
    public Persona getPersona() { return persona; }
    public Evento getEvento() { return evento; }
    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
    // Setters correspondientes
}