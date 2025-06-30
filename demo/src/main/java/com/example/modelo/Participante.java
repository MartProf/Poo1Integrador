package com.example.modelo;
import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Participante")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Participante {
    @Id
    private int dni;
    Persona persona;
    Evento evento;
    LocalDate fechaincripción;
    
}
    