package com.example.modelo;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Concierto")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Concierto extends Evento {

    @ManyToMany
    @JoinTable(
        name = "concierto_artistas",
        joinColumns = @JoinColumn(name = "concierto_id"), inverseJoinColumns = @JoinColumn(name = "persona_id"))
    private List <Persona> artistas;
    
    private boolean entradaGratuita;

}
    