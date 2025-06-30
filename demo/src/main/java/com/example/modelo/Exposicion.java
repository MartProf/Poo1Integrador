package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

    @OneToOne
    @JoinColumn(name = "curador_id")
    private Persona curador;

    
}
