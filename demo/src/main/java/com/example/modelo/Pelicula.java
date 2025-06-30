package com.example.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "pelicula")
public class Pelicula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titulo;
    private int orden;
    
    // Getters y setters
}