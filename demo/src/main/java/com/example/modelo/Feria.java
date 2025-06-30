package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Feria")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Feria extends Evento {
    
    private int cantidadDeStand;
    private boolean alAirelibre;
}