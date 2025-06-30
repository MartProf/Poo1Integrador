package com.example.modelo;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="CicloDeCine")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class CicloDeCine extends Evento {
    
    private boolean hatCharlas;
    private List<Pelicula> peliculas;
}