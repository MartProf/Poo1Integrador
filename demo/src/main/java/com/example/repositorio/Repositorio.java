package com.example.repositorio;

import com.example.modelo.Persona;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

public class Repositorio {

    private final EntityManager em;

    public Repositorio(EntityManagerFactory emf) {
        if (!emf.isOpen()) {
            throw new IllegalArgumentException("EntityManagerFactory está cerrado.");
        }
        this.em = emf.createEntityManager();
    }

    public Persona buscarPersonaPorUsuarioYContrasena(String usuario, String contrasena) {
        TypedQuery<Persona> query = em.createQuery(
            "SELECT p FROM Persona p WHERE p.usuario = :usuario AND p.contrasena = :contrasena",
            Persona.class
        );
        query.setParameter("usuario", usuario);
        query.setParameter("contrasena", contrasena);

        return query.getResultStream().findFirst().orElse(null);
    }
    
    public void guardarPersona(Persona persona) {
        em.getTransaction().begin();
        em.persist(persona);
        em.getTransaction().commit();
    }
}
