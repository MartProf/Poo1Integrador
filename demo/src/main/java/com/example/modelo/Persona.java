package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="personas")
public class Persona {
    @Id
    private int dni;
    private String nombre;
    private String apellido;
    private String telefono;
    private String gmail;
    
}
    