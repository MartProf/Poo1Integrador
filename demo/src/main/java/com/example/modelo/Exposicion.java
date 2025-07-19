package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Exposicion")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Exposicion extends Evento {
    
    private String tipoArte;

    @ManyToOne
    @JoinColumn(name = "curador_id", unique = false)
    private Persona curador;

    
}
