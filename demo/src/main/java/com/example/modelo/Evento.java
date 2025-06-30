package com.example.modelo;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name="Evento")
@AllArgsConstructor
@NoArgsConstructor
public abstract class Evento {
    @Id
    private int id;
    private String nombre;
    private LocalDate fechaInicio;
    private int duraciónDias;
    private EstadoEvento estado;
    private List<Persona> responsables;
    private List<Persona> participantes;
}
    