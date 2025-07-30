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

    // Método helper que proporciona un EntityManager nuevo para conectarse con la base de datos
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    // Devuelve todos los eventos sin cargar relaciones
    public List<Evento> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Evento e", Evento.class).getResultList();
        } finally {
            em.close(); // Siempre cerrar el EntityManager
        }
    }

    // Devuelve todos los eventos junto con sus relaciones (responsables, participantes, artistas, películas)
    public List<Evento> findAllWithRelations() {
        EntityManager em = getEntityManager();
        try {
            // Consulta 1: cargar eventos con sus responsables (LEFT JOIN FETCH)
            TypedQuery<Evento> query1 = em.createQuery(
                    "SELECT DISTINCT e FROM Evento e " +
                    "LEFT JOIN FETCH e.responsables",
                    Evento.class
            );
            List<Evento> eventos = query1.getResultList();

            // Si hay eventos, proceder a cargar otras relaciones
            if (!eventos.isEmpty()) {
                // Consulta 2: cargar participantes
                TypedQuery<Evento> query2 = em.createQuery(
                        "SELECT DISTINCT e FROM Evento e " +
                        "LEFT JOIN FETCH e.participantes " +
                        "WHERE e IN :eventos",
                        Evento.class
                );
                query2.setParameter("eventos", eventos);
                query2.getResultList(); // Forzar carga en caché

                // Extraer IDs para siguientes consultas
                List<Integer> eventosIds = eventos.stream()
                    .map(Evento::getId)
                    .collect(java.util.stream.Collectors.toList());

                // Consulta 3: cargar artistas para conciertos
                TypedQuery<Evento> query3 = em.createQuery(
                        "SELECT DISTINCT c FROM Concierto c " +
                        "LEFT JOIN FETCH c.artistas " +
                        "WHERE c.id IN :eventosIds",
                        Evento.class
                );
                query3.setParameter("eventosIds", eventosIds);
                query3.getResultList(); // Carga en caché

                // Consulta 4: cargar películas para ciclos de cine
                TypedQuery<Evento> query4 = em.createQuery(
                        "SELECT DISTINCT cc FROM CicloDeCine cc " +
                        "LEFT JOIN FETCH cc.peliculas " +
                        "WHERE cc.id IN :eventosIds",
                        Evento.class
                );
                query4.setParameter("eventosIds", eventosIds);
                query4.getResultList(); // Carga en caché
            }

            return eventos;
        } finally {
            em.close();
        }
    }

    // Retorna solo los eventos confirmados y carga todas sus relaciones asociadas
    public List<Evento> findAllDisponibles() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Evento> query = em.createQuery(
                    "SELECT e FROM Evento e WHERE e.estado = :estado",
                    Evento.class
            );
            query.setParameter("estado", EstadoEvento.CONFIRMADO);
            List<Evento> eventos = query.getResultList();

            // Forzar carga de relaciones para cada evento (evita LazyInitializationException)
            for (Evento evento : eventos) {
                evento.getResponsables().size();     // Cargar responsables
                evento.getParticipantes().size();    // Cargar participantes

                // Cargar relaciones específicas según el tipo de evento
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

    // Devuelve los eventos en los que la persona especificada es responsable
    public List<Evento> findByResponsable(Persona responsable) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Evento> query = em.createQuery(
                    "SELECT e FROM Evento e JOIN e.responsables r WHERE r = :responsable",
                    Evento.class
            );
            query.setParameter("responsable", responsable);
            List<Evento> eventos = query.getResultList();

            // Forzar carga de relaciones para cada evento
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

    // Busca un evento por su ID y carga todas sus relaciones si lo encuentra
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

    // Guarda un evento nuevo o actualiza uno existente (según si tiene ID)
    public void save(Evento evento) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (evento.getId() == 0) {
                em.persist(evento); // Evento nuevo
            } else {
                em.merge(evento);   // Evento existente, se actualiza
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback(); // Reversión si ocurre error
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // Actualiza un evento existente
    public void actualizarEvento(Evento evento) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(evento); // Actualiza el evento
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback(); // Revierte en caso de error
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
            // Si la entidad no está en el contexto de persistencia, se adjunta
            Evento eventoToDelete = em.contains(evento) ? evento : em.merge(evento);
            em.remove(eventoToDelete); // Elimina el evento
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback(); // Deshace si ocurre error
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // Verifica si una persona es responsable de un evento
    public boolean esResponsable(Persona persona, Evento evento) {
        if (persona == null || evento == null) {
            return false; // Validación de parámetros
        }

        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(e) FROM Evento e JOIN e.responsables r WHERE e.id = :eventoId AND r.dni = :personaDni",
                Long.class
            ).setParameter("eventoId", evento.getId())
             .setParameter("personaDni", persona.getDni())
             .getSingleResult();
            return count > 0; // Retorna true si existe al menos una coincidencia
        } finally {
            em.close();
        }
    }
}
