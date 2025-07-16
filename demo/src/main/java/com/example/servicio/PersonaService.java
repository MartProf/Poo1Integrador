package com.example.servicio;

import java.util.List;

import com.example.modelo.Persona;
import com.example.repositorio.PersonaRepository;

import jakarta.persistence.EntityManager;

public class PersonaService {

    private PersonaRepository personaRepo;

    public PersonaService(EntityManager em) {
        this.personaRepo = new PersonaRepository(em);
    }

    public Persona buscarPorDni(int dni) {
        return personaRepo.findByDni(dni);
    }

    public List<Persona> buscarPorNombre(String nombre) {
        return personaRepo.findByNombreContaining(nombre);
    }

    public void registrarPersona(Persona persona) {
        if (persona.getNombre() == null || persona.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la persona no puede estar vacío.");
        }
        personaRepo.save(persona);
    }
}

