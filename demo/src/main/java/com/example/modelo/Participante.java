package com.example.modelo;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="participante")
public class Participante {
    @Id
    private int dni;
   // Persona persona;
    Evento evento;
    LocalDate fechaincripción;
    
}
    