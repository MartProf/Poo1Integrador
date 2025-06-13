package com.example.modelo;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Concierto")
public class Concierto {
    @Id
    //private List <Persona>artistas;
    private boolean entradaGratuita;

    private String lugar;
   
}
    