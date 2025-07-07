package com.example.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "taller")
public class Taller extends Evento implements TieneCupo {
    private int cupoMaximo;
    
    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Persona instructor;
    
    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;
    
    @Override
    public int getCupoMaximo() {
        return cupoMaximo;
    }
    
    @Override
    public int getCupoDisponible() {
        return cupoMaximo - (getParticipantes() != null ? getParticipantes().size() : 0);
    }
    
    // Getters y setters
    public Persona getInstructor() {
        return instructor;
    }

    public void setInstructor(Persona instructor) {
        this.instructor = instructor;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }
}