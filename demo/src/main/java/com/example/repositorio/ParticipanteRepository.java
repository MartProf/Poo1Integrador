package com.example.repositorio;

import com.example.modelo.Evento;
import com.example.modelo.Participante;
import com.example.modelo.Persona;
import com.example.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class ParticipanteRepository {

    // Método helper para obtener EntityManager fresco
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    public void save(Participante participante) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(participante);
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

    public boolean estaInscripto(Persona persona, Evento evento) {
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(p) FROM Participante p WHERE p.persona = :persona AND p.evento = :evento",
                Long.class
            ).setParameter("persona", persona)
             .setParameter("evento", evento)
             .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }
}
