package com.example.repositorio;

import com.example.modelo.Evento;
import com.example.modelo.EstadoEvento;
import com.example.modelo.Persona;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class EventoRepository {

    private EntityManager em;

    public EventoRepository(EntityManager em) {
        this.em = em;
    }

    public List<Evento> findAll() {
        return em.createQuery("SELECT e FROM Evento e", Evento.class).getResultList();
    }

    public List<Evento> findAllDisponibles() {
        // Solo eventos que estén CONFIRMADOS
        TypedQuery<Evento> query = em.createQuery(
                "SELECT e FROM Evento e WHERE e.estado = :estado",
                Evento.class
        );
        query.setParameter("estado", EstadoEvento.CONFIRMADO);
        return query.getResultList();
    }

    public List<Evento> findByResponsable(Persona responsable) {
        // Devuelve los eventos donde la persona es responsable
        TypedQuery<Evento> query = em.createQuery(
                "SELECT e FROM Evento e JOIN e.responsables r WHERE r = :responsable",
                Evento.class
        );
        query.setParameter("responsable", responsable);
        return query.getResultList();
    }

    public Evento findById(int id) {
        return em.find(Evento.class, id);
    }

    public void save(Evento evento) {
        if (evento.getId() == 0) {
            em.persist(evento);
        } else {
            em.merge(evento);
        }
    }

    public void delete(Evento evento) {
        em.remove(em.contains(evento) ? evento : em.merge(evento));
    }
}
