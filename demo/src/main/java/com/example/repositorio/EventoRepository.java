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

    // Método helper para obtener EntityManager fresco
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    public List<Evento> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Evento e", Evento.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Evento> findAllWithRelations() {
        EntityManager em = getEntityManager();
        try {
            // Primer query: cargar eventos con responsables
            TypedQuery<Evento> query1 = em.createQuery(
                    "SELECT DISTINCT e FROM Evento e " +
                    "LEFT JOIN FETCH e.responsables",
                    Evento.class
            );
            List<Evento> eventos = query1.getResultList();
            
            // Segundo query: cargar participantes para los eventos obtenidos
            if (!eventos.isEmpty()) {
                TypedQuery<Evento> query2 = em.createQuery(
                        "SELECT DISTINCT e FROM Evento e " +
                        "LEFT JOIN FETCH e.participantes " +
                        "WHERE e IN :eventos",
                        Evento.class
                );
                query2.setParameter("eventos", eventos);
                query2.getResultList(); // Esto carga los participantes en caché
                
                // Tercer query: cargar artistas para los conciertos
                // Filtrar por ID para evitar problemas de conversión de tipos
                List<Integer> eventosIds = eventos.stream()
                    .map(Evento::getId)
                    .collect(java.util.stream.Collectors.toList());
                
                TypedQuery<Evento> query3 = em.createQuery(
                        "SELECT DISTINCT c FROM Concierto c " +
                        "LEFT JOIN FETCH c.artistas " +
                        "WHERE c.id IN :eventosIds",
                        Evento.class
                );
                query3.setParameter("eventosIds", eventosIds);
                query3.getResultList(); // Esto carga los artistas en caché
                
                // Cuarto query: cargar películas para los ciclos de cine
                TypedQuery<Evento> query4 = em.createQuery(
                        "SELECT DISTINCT cc FROM CicloDeCine cc " +
                        "LEFT JOIN FETCH cc.peliculas " +
                        "WHERE cc.id IN :eventosIds",
                        Evento.class
                );
                query4.setParameter("eventosIds", eventosIds);
                query4.getResultList(); // Esto carga las películas en caché
            }
            
            return eventos;
        } finally {
            em.close();
        }
    }

    public List<Evento> findAllDisponibles() {
        EntityManager em = getEntityManager();
        try {
            // Solo eventos que estén CONFIRMADOS
            TypedQuery<Evento> query = em.createQuery(
                    "SELECT e FROM Evento e WHERE e.estado = :estado",
                    Evento.class
            );
            query.setParameter("estado", EstadoEvento.CONFIRMADO);
            List<Evento> eventos = query.getResultList();
            
            // Inicializar las colecciones lazy dentro de la sesión
            for (Evento evento : eventos) {
                evento.getResponsables().size(); // Forzar carga de responsables
                evento.getParticipantes().size(); // Forzar carga de participantes
                
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

    public List<Evento> findByResponsable(Persona responsable) {
        EntityManager em = getEntityManager();
        try {
            // Devuelve los eventos donde la persona es responsable
            TypedQuery<Evento> query = em.createQuery(
                    "SELECT e FROM Evento e JOIN e.responsables r WHERE r = :responsable",
                    Evento.class
            );
            query.setParameter("responsable", responsable);
            List<Evento> eventos = query.getResultList();
            
            // Inicializar las colecciones lazy dentro de la sesión
            for (Evento evento : eventos) {
                evento.getResponsables().size(); // Forzar carga de responsables
                evento.getParticipantes().size(); // Forzar carga de participantes
                
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

    public Evento findById(int id) {
        EntityManager em = getEntityManager();
        try {
            Evento evento = em.find(Evento.class, id);
            if (evento != null) {
                // Inicializar las colecciones lazy dentro de la sesión
                evento.getResponsables().size(); // Forzar carga de responsables
                evento.getParticipantes().size(); // Forzar carga de participantes
                
                // Cargar relaciones específicas según el tipo de evento
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

    public void save(Evento evento) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (evento.getId() == 0) {
                em.persist(evento);
            } else {
                em.merge(evento);
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

    // Nuevo método: verificar si una persona es responsable de un evento
    public boolean esResponsable(Persona persona, Evento evento) {
        // Validar que los parámetros no sean null
        if (persona == null || evento == null) {
            return false;
        }
        
        EntityManager em = getEntityManager();
        try {
            Long count = em.createQuery(
                "SELECT COUNT(e) FROM Evento e JOIN e.responsables r WHERE e.id = :eventoId AND r.dni = :personaDni",
                Long.class
            ).setParameter("eventoId", evento.getId())
             .setParameter("personaDni", persona.getDni())
             .getSingleResult();
            
            return count > 0;
        } finally {
            em.close();
        }
    }
}
