package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Taller extends Evento implements TieneCupo{
    
    private int cupoMaximo;

    @ManyToOne
    @JoinColumn(name = "instructor_id", unique = false)
    private Persona instructor;

    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;
    
    @Override
    public int getCupoMaximo() {
        return cupoMaximo;
    }

    @Override
    public int getCupoDisponible() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCupoDisponible'");
    }

    
}
