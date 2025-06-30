package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Pelicula")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pelicula {
    
    private String titulo;
    private int orden;;
}
