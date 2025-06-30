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
    
    private LocalDate fechaInscripcion; // Corregir nombre
    
    // Getters y setters
}
    