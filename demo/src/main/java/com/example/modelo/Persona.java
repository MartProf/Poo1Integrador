package com.example.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "personas")
public class Persona {
    @Id
    private int dni;
    
    private String nombreCompleto;
    private String telefono;
    private String email;
    
    // Getters y setters
}