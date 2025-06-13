package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Feria")
public class Feria {
    @Id
    private int cantidaddeStand;
    private boolean alAirelibre;
    
}