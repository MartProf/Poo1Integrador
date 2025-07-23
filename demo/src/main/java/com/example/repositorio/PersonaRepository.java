package com.example.repositorio;

import java.util.List;

import com.example.modelo.Persona;
import com.example.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class PersonaRepository {

    // Método helper para obtener EntityManager fresco
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    public Persona findByDni(int dni) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Persona.class, dni);
        } finally {
            em.close();
        }
    }

    public Persona findByDniWithParticipaciones(int dni) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Persona p LEFT JOIN FETCH p.participaciones WHERE p.dni = :dni",
                Persona.class
            ).setParameter("dni", dni)
             .getResultStream()
             .findFirst()
             .orElse(null);
        } finally {
            em.close();
        }
    }

    public List<Persona> findByNombreContaining(String nombre) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Persona p WHERE LOWER(p.nombre) LIKE LOWER(:filtro)",
                Persona.class
            ).setParameter("filtro", "%" + nombre + "%").getResultList();
        } finally {
            em.close();
        }
    }

    public void save(Persona persona) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (em.find(Persona.class, persona.getDni()) == null) {
                em.persist(persona);
            } else {
                em.merge(persona);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void actualizar(Persona persona) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(persona);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean existeUsuario(String usuario) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(p) FROM Persona p WHERE p.usuario = :usuario", 
                Long.class
            ).setParameter("usuario", usuario).getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public boolean existeEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(p) FROM Persona p WHERE p.email = :email", 
                Long.class
            ).setParameter("email", email).getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public boolean existeDni(int dni) {
        return findByDni(dni) != null;
    }

    public Persona buscarPersonaPorUsuarioYContrasena(String usuario, String contrasena) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Persona p WHERE p.usuario = :usuario AND p.contrasena = :contrasena",
                Persona.class
            ).setParameter("usuario", usuario)
             .setParameter("contrasena", contrasena)
             .getResultStream()
             .findFirst()
             .orElse(null);
        } finally {
            em.close();
        }
    }
}
