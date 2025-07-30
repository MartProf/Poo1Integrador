package com.example.repositorio;

import com.example.modelo.Evento;
import com.example.modelo.EstadoEvento;
import com.example.modelo.Persona;
import com.example.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class EventoRepository {

    // Método auxiliar para obtener una instancia de EntityManager (conexión a la BD)
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    // Recupera todos los eventos (sin cargar relaciones)
    public List<Evento> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Evento e", Evento.class).getResultList();
        } finally {
            em.close();
        }
    }

    // Recupera todos los eventos y todas sus relaciones asociadas (JOIN FETCH)
    public List<Evento> findAllWithRelations() {
        EntityManager em = getEntityManager();
        try {
            // Cargar responsables
            TypedQuery<Evento> query1 = em.createQuery(
                "SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.responsables", Evento.class);
            List<Evento> eventos = query1.getResultList();

            if (!eventos.isEmpty()) {
                // Cargar participantes
                TypedQuery<Evento> query2 = em.createQuery(
                    "SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.participantes WHERE e IN :eventos",
                    Evento.class);
                query2.setParameter("eventos", eventos);
                query2.getResultList();

                // Obtener IDs
                List<Integer> eventosIds = eventos.stream()
                    .map(Evento::getId)
                    .collect(java.util.stream.Collectors.toList());

                // Cargar artistas (para conciertos)
                TypedQuery<Evento> query3 = em.createQuery(
                    "SELECT DISTINCT c FROM Concierto c LEFT JOIN FETCH c.artistas WHERE c.id IN :eventosIds",
                    Evento.class);
                query3.setParameter("eventosIds", eventosIds);
                query3.getResultList();

                // Cargar películas (para ciclos de cine)
                TypedQuery<Evento> query4 = em.createQuery(
                    "SELECT DISTINCT cc FROM CicloDeCine cc LEFT JOIN FETCH cc.peliculas WHERE cc.id IN :eventosIds",
                    Evento.class);
                query4.setParameter("eventosIds", eventosIds);
                query4.getResultList();
            }

            return eventos;
        } finally {
            em.close();
        }
    }

    // Devuelve los eventos que están CONFIRMADOS y carga todas sus relaciones
    public List<Evento> findAllDisponibles() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Evento> query = em.createQuery(
                "SELECT e FROM Evento e WHERE e.estado = :estado", Evento.class);
            query.setParameter("estado", EstadoEvento.CONFIRMADO);
            List<Evento> eventos = query.getResultList();

            // Cargar relaciones para evitar errores con Lazy Loading
            for (Evento evento : eventos) {
                evento.getResponsables().size();
                evento.getParticipantes().size();
                if (evento instanceof com.example.modelo.Concierto) {
                    ((com.example.modelo.Concierto) evento).getArtistas().size();
                } else if (evento instanceof com.example.modelo.CicloDeCine) {
                    ((com.example.modelo.CicloDeCine) evento).getPeliculas().size();
                }
            }

            return eventos;
        } finally {
            em.close();
        }
    }

    // Devuelve los eventos en los que una persona es responsable
    public List<Evento> findByResponsable(Persona responsable) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Evento> query = em.createQuery(
                "SELECT e FROM Evento e JOIN e.responsables r WHERE r = :responsable", Evento.class);
            query.setParameter("responsable", responsable);
            List<Evento> eventos = query.getResultList();

            for (Evento evento : eventos) {
                evento.getResponsables().size();
                evento.getParticipantes().size();
                if (evento instanceof com.example.modelo.Concierto) {
                    ((com.example.modelo.Concierto) evento).getArtistas().size();
                } else if (evento instanceof com.example.modelo.CicloDeCine) {
                    ((com.example.modelo.CicloDeCine) evento).getPeliculas().size();
                }
            }

            return eventos;
        } finally {
            em.close();
        }
    }

    // Busca un evento por ID y carga sus relaciones si lo encuentra
    public Evento findById(int id) {
        EntityManager em = getEntityManager();
        try {
            Evento evento = em.find(Evento.class, id);
            if (evento != null) {
                evento.getResponsables().size();
                evento.getParticipantes().size();
                if (evento instanceof com.example.modelo.Concierto) {
                    ((com.example.modelo.Concierto) evento).getArtistas().size();
                } else if (evento instanceof com.example.modelo.CicloDeCine) {
                    ((com.example.modelo.CicloDeCine) evento).getPeliculas().size();
                }
            }
            return evento;
        } finally {
            em.close();
        }
    }

    // Guarda un nuevo evento o actualiza uno existente (persist vs merge)
    public void save(Evento evento) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (evento.getId() == 0) {
                em.persist(evento); // Evento nuevo
            } else {
                em.merge(evento);   // Evento existente
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

    // Actualiza un evento ya existente
    public void actualizarEvento(Evento evento) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(evento);
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

    // Elimina un evento de la base de datos
    public void delete(Evento evento) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Evento eventoToDelete = em.contains(evento) ? evento : em.merge(evento);
            em.remove(eventoToDelete);
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

    // Verifica si una persona es responsable de un evento
    public boolean esResponsable(Persona persona, Evento evento) {
        if (persona == null || evento == null) return false;

        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(e) FROM Evento e JOIN e.responsables r " +
                "WHERE e.id = :eventoId AND r.dni = :personaDni", Long.class)
                .setParameter("eventoId", evento.getId())
                .setParameter("personaDni", persona.getDni())
                .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}
