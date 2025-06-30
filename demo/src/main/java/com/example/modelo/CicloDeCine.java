package com.example.modelo;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "ciclo_cine")
public class CicloDeCine extends Evento {
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ciclo_id")
    private List<Pelicula> peliculas;
    
    private boolean hayCharlas;
    
    
}