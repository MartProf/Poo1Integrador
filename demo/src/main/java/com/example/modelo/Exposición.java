package com.example.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "exposicion")
public class Exposicion extends Evento {
    private String tipoArte;
    
    @ManyToOne
    @JoinColumn(name = "curador_id")
    private Persona curador;
    
    
}