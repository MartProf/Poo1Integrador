package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Taller")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Taller extends Evento {
    
    private int cupoMaximo;
    private Persona instructor;
    private Modalidad modalidad;


}
