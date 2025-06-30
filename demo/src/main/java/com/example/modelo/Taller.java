package com.example.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "taller")
public class Taller extends Evento implements TieneCupo {
    private int cupoMaximo;
    
    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Persona instructor;
    
    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;
    
    @Override
    public int getCupoMaximo() {
        return cupoMaximo;
    }
    
    @Override
    public int getCupoDisponible() {
        return cupoMaximo - getParticipantes().size();
    }
    
    // Getters y setters
}