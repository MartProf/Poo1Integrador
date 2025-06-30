package com.example.modelo;
import java.time.LocalDate;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "evento")
public abstract class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private LocalDate fechaInicio;
    private int duracionDias;
    
    
}
    