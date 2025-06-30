package com.example.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "feria")
public class Feria extends Evento {
    private int cantidadStands;  // Corregir nombre (de cantidaddeStand)
    private boolean alAireLibre; // Corregir nombre (de alAirelibre)
    
} 