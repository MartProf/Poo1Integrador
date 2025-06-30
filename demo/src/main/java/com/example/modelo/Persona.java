package com.example.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Persona")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Persona {
    @Id
    private int dni;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    
}
    