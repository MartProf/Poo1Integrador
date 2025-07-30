package com.example.repositorio;

import com.example.modelo.Evento;
import com.example.modelo.Participante;
import com.example.modelo.Persona;
import com.example.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class ParticipanteRepository {

    // Método helper para obtener un EntityManager (administrador de la conexión con la base de datos)
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    // Guarda una nueva instancia de Participante en la base de datos
    public void save(Participante participante) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction(); // Manejo de transacciones
        try {
            tx.begin(); // Inicia la transacción
            em.persist(participante); // Persiste (inserta) el objeto participante
            tx.commit(); // Confirma la transacción
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback(); // Revierte la transacción en caso de error
            }
            throw e; // Lanza la excepción para ser manejada más arriba
        } finally {
            em.close(); // Cierra el EntityManager y libera los recursos
        }
    }

    // Verifica si una persona ya está inscripta en un evento específico
    public boolean estaInscripto(Persona persona, Evento evento) {
        // Validación previa: si alguno de los parámetros es null, retorna false directamente
        if (persona == null || evento == null) {
            return false;
        }

        EntityManager em = getEntityManager();
        try {
            // Consulta que cuenta cuántos registros existen con esa persona y evento
            Long count = em.createQuery(
                "SELECT COUNT(p) FROM Participante p WHERE p.persona = :persona AND p.evento = :evento",
                Long.class
            ).setParameter("persona", persona) // Asocia el parámetro persona a la query
             .setParameter("evento", evento)   // Asocia el parámetro evento a la query
             .getSingleResult(); // Obtiene el resultado como un único número

            // Retorna true si hay al menos una coincidencia (está inscripto)
            return count != null && count > 0;
        } finally {
            em.close(); // Cierra el EntityManager
        }
    }
}
