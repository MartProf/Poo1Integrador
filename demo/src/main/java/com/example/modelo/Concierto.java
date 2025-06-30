package com.example.modelo;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Concierto")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Concierto extends Evento {


    private List <Persona> artistas;
    private boolean entradaGratuita;

}
    