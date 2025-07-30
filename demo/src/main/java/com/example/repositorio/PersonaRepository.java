package com.example.repositorio;

import java.util.List;

import com.example.modelo.Persona;
import com.example.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class PersonaRepository {

    // Método helper para obtener un EntityManager (gestiona la conexión con la base de datos)
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    // Busca una persona por su DNI utilizando el método 'find'
    public Persona findByDni(int dni) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Persona.class, dni); // Búsqueda por clave primaria
        } finally {
            em.close(); // Cierre del EntityManager para liberar recursos
        }
    }

    // Busca una persona por DNI e incluye sus participaciones (uso de FETCH JOIN para evitar lazy loading)
    public Persona findByDniWithParticipaciones(int dni) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Persona p LEFT JOIN FETCH p.participaciones WHERE p.dni = :dni",
                Persona.class
            ).setParameter("dni", dni)
             .getResultStream()
             .findFirst()
             .orElse(null); // Devuelve la persona si existe, si no devuelve null
        } finally {
            em.close();
        }
    }

    // Busca personas cuyo nombre contenga una cadena (no distingue mayúsculas/minúsculas)
    public List<Persona> findByNombreContaining(String nombre) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Persona p WHERE LOWER(p.nombre) LIKE LOWER(:filtro)",
                Persona.class
            ).setParameter("filtro", "%" + nombre + "%")
             .getResultList(); // Devuelve lista de personas que coincidan
        } finally {
            em.close();
        }
    }

    // Devuelve todas las personas ordenadas por apellido y nombre
    public List<Persona> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Persona p ORDER BY p.apellido, p.nombre", Persona.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Guarda una persona: si no existe, la persiste; si ya existe, la actualiza con merge
    public void save(Persona persona) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction(); // Maneja la transacción
        try {
            tx.begin();
            if (em.find(Persona.class, persona.getDni()) == null) {
                em.persist(persona); // Inserta nueva persona
            } else {
                em.merge(persona); // Actualiza persona existente
            }
            tx.commit(); // Confirma los cambios
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback(); // Revierte cambios si ocurre un error
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // Actualiza una persona existente con merge
    public void actualizar(Persona persona) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(persona); // Actualiza el estado de la persona en la BD
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback(); // Revierte si hay error
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // Verifica si ya existe un usuario con ese nombre en la base de datos
    public boolean existeUsuario(String usuario) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(p) FROM Persona p WHERE p.usuario = :usuario", 
                Long.class
            ).setParameter("usuario", usuario)
             .getSingleResult();
            return count > 0; // Retorna true si hay al menos un usuario
        } finally {
            em.close();
        }
    }

    // Verifica si ya existe un email registrado
    public boolean existeEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(p) FROM Persona p WHERE p.email = :email", 
                Long.class
            ).setParameter("email", email)
             .getSingleResult();
            return count > 0; // Retorna true si ya existe el email
        } finally {
            em.close();
        }
    }

    // Verifica si existe una persona con un DNI específico
    public boolean existeDni(int dni) {
        return findByDni(dni) != null;
    }

    // Busca una persona por su nombre de usuario y contraseña (para autenticación)
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
             .orElse(null); // Retorna la persona si coincide usuario y contraseña
        } finally {
            em.close();
        }
    }
}
