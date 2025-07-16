package com.example.repositorio;

import java.util.List;

import com.example.modelo.Persona;

import jakarta.persistence.EntityManager;

public class PersonaRepository {
    private EntityManager em;

    public PersonaRepository(EntityManager em) {
        this.em = em;
    }

    public Persona findByDni(int dni) {
        return em.find(Persona.class, dni);
    }

    public List<Persona> findByNombreContaining(String nombre) {
        return em.createQuery(
            "SELECT p FROM Persona p WHERE LOWER(p.nombre) LIKE LOWER(:filtro)",
            Persona.class
        ).setParameter("filtro", "%" + nombre + "%").getResultList();
    }

    public void save(Persona persona) {
        if (em.find(Persona.class, persona.getDni()) == null) {
            em.persist(persona);
        } else {
            em.merge(persona);
        }
    }
}
