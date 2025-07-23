package com.example.modelo;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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
    private String usuario;      
    private String contrasena;   

    // Eventos donde actúa como responsable
    @ManyToMany(mappedBy = "responsables")
    private List<Evento> eventosResponsable;

    // Eventos donde actúa como artista
    @ManyToMany(mappedBy = "artistas")
    private List<Concierto> conciertosComoArtista;

    // Exposiciones donde actúa como curador
    @OneToMany(mappedBy = "curador")
    private List<Exposicion> exposicionesComoCurador;

    // Talleres donde actúa como instructor
    @OneToMany(mappedBy = "instructor")
    private List<Taller> talleresComoInstructor;

    // Participaciones como asistente/inscripto
    @OneToMany(mappedBy = "persona")
    private List<Participante> participaciones;

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}
    