package com.example.repositorio;

import com.example.modelo.Participante;
import jakarta.persistence.EntityManager;

public class ParticipanteRepository {

    private EntityManager em;

    public ParticipanteRepository(EntityManager em) {
        this.em = em;
    }

    public void save(Participante participante) {
        em.getTransaction().begin();
        em.persist(participante);
        em.getTransaction().commit();
    }
}
