package com.example.modelo;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="evento")
public class Evento {
    @Id
    private int id;
    private String nombre;
    private LocalDate fechainicio;
    private int duración;
    
   
}
    