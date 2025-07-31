package com.example.repositorio;

import com.example.modelo.Evento;
import com.example.modelo.Participante;
import com.example.modelo.Persona;
import com.example.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Repositorio especializado para operaciones de persistencia de participantes.
 * 
 * Esta clase maneja la persistencia y consultas específicas para la entidad
 * Participante, que representa la relación many-to-many entre Persona y Evento.
 * Implementa operaciones fundamentales para el sistema de inscripciones,
 * incluyendo registro de nuevos participantes y verificación de estado.
 * 
 * Responsabilidades Principales:
 * - Persistencia de nuevas inscripciones (relaciones Persona-Evento)
 * - Verificación de estado de inscripción existente
 * - Gestión de transacciones para operaciones de escritura
 * - Validación de parámetros para prevenir estados inconsistentes
 * 
 * Arquitectura de Datos:
 * - Maneja entidad de asociación Participante
 * - Gestiona relación many-to-many entre Persona y Evento
 * - Incluye metadatos adicionales como fecha de inscripción
 * - Implementa patrón EntityManager per Operation
 * 
 * Operaciones Implementadas:
 * - Creación: Registro de nuevas inscripciones con transacción completa
 * - Consulta: Verificación eficiente de inscripciones existentes
 * - Validación: Prevención de parámetros null y estados inválidos
 * 
 * Estrategias de Consulta:
 * - COUNT queries para verificación de existencia eficiente
 * - Parámetros tipados para prevenir SQL injection
 * - Validación preventiva para evitar consultas innecesarias
 * - Gestión de recursos con cierre automático de EntityManager
 * 
 * Integración con el Sistema:
 * - Utilizado por ParticipanteService para lógica de inscripciones
 * - Coordina con EventoRepository para datos de eventos
 * - Trabaja con PersonaRepository para validación de personas
 * - Base para implementación de reglas de negocio de inscripción
 * 
 * Patrones de Diseño:
 * - Repository Pattern: Encapsulación de acceso a datos
 * - EntityManager per Operation: Aislamiento de operaciones
 * - Validation Pattern: Verificación preventiva de parámetros
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2024
 * 
 * @see com.example.modelo.Participante
 * @see com.example.modelo.Persona
 * @see com.example.modelo.Evento
 * @see ParticipanteService
 * @see com.example.util.JpaUtil
 */
public class ParticipanteRepository {

    /**
     * Método helper que proporciona un EntityManager nuevo para operaciones de persistencia.
     * 
     * Implementa el patrón "EntityManager per Operation" específicamente
     * diseñado para operaciones simples y directas. Esta estrategia es
     * especialmente apropiada para el repositorio de participantes que
     * maneja operaciones atómicas de inscripción y consulta.
     * 
     * Características del Patrón:
     * - Cada operación obtiene EntityManager fresco e independiente
     * - Aislamiento completo entre operaciones concurrentes
     * - Gestión explícita del ciclo de vida del EntityManager
     * - Contexto de persistencia limpio para cada transacción
     * 
     * @return Nueva instancia de EntityManager configurada para el sistema
     * 
     * @implNote Delegación a JpaUtil para configuración centralizada
     * @implNote Responsabilidad del caller cerrar el EntityManager
     */
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    /**
     * Persiste una nueva inscripción de participante en la base de datos.
     * 
     * Este método maneja la creación transaccional de nuevas relaciones
     * Persona-Evento a través de la entidad Participante. Implementa
     * gestión completa de transacciones con rollback automático para
     * asegurar consistencia de datos en el sistema de inscripciones.
     * 
     * Proceso Transaccional:
     * 1. Inicio de transacción explícita
     * 2. Persistencia de la entidad Participante
     * 3. Commit para hacer permanente la inscripción
     * 4. Rollback automático en caso de cualquier error
     * 5. Liberación garantizada de recursos
     * 
     * Operación de Persistencia:
     * - Utiliza em.persist() para entidades nuevas
     * - Hibernate genera ID automáticamente
     * - Se establecen relaciones con Persona y Evento
     * - Fecha de inscripción se persiste según configuración
     * 
     * Manejo de Errores Robusto:
     * - Detección automática de transacciones activas
     * - Rollback solo si la transacción está en progreso
     * - Propagación de excepciones para manejo en capas superiores
     * - Liberación de recursos garantizada independiente del resultado
     * 
     * Casos de Uso:
     * - Registro de nuevas inscripciones desde interfaz pública
     * - Inscripciones automáticas del sistema
     * - Operaciones de importación de participantes
     * - Procesos batch de inscripción masiva
     * 
     * Validaciones Previas Recomendadas:
     * - Verificar que Persona y Evento existan
     * - Validar que no existe inscripción previa (usar estaInscripto)
     * - Confirmar que evento acepta inscripciones
     * - Verificar disponibilidad de cupos si aplica
     * 
     * @param participante Entidad Participante completamente configurada con
     *                    Persona, Evento y fecha de inscripción establecidos
     * 
     * @throws Exception Si ocurre error durante la persistencia, incluyendo:
     *                  - Violaciones de constraints de base de datos
     *                  - Problemas de conectividad
     *                  - Errores de validación de JPA
     *                  - Conflictos de transacción
     * 
     * @implNote La entidad debe tener Persona y Evento configurados antes de persistir
     * @implNote Las validaciones de negocio deben realizarse en la capa de servicio
     */
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

    /**
     * Verifica si una persona ya está inscrita en un evento específico.
     * 
     * Implementa consulta de verificación optimizada utilizando COUNT para
     * determinar eficientemente la existencia de una inscripción sin cargar
     * entidades completas. Incluye validaciones defensivas y manejo robusto
     * de casos edge para prevenir estados inconsistentes.
     * 
     * Estrategia de Verificación Eficiente:
     * - Consulta COUNT para máxima eficiencia (solo retorna número)
     * - Sin carga innecesaria de entidades Participante completas
     * - Utiliza índices de base de datos para búsqueda rápida
     * - Validación preventiva de parámetros null
     * 
     * Consulta JPQL Optimizada:
     * "SELECT COUNT(p) FROM Participante p WHERE p.persona = :persona AND p.evento = :evento"
     * 
     * Elementos Clave de la Consulta:
     * - COUNT(p): Retorna solo el número de coincidencias
     * - Filtro por persona: Utiliza relación directa con entidad Persona
     * - Filtro por evento: Utiliza relación directa con entidad Evento
     * - Parámetros tipados: Previene SQL injection y mejora performance
     * 
     * Validaciones Defensivas:
     * - Verificación persona null → retorno inmediato false
     * - Verificación evento null → retorno inmediato false
     * - Verificación count null → protección contra resultados inesperados
     * - Sin excepciones por parámetros inválidos
     * 
     * Casos de Uso Principales:
     * - Prevención de inscripciones duplicadas
     * - Validación previa a mostrar botones de inscripción
     * - Verificación de estado en interfaces de usuario
     * - Auditoría de participación en reportes
     * - Implementación de reglas de negocio de inscripción
     * 
     * Ventajas de Performance:
     * - Consulta extremadamente rápida (solo COUNT)
     * - Utiliza índices sobre relaciones FK
     * - Sin transferencia de datos innecesarios
     * - Mínimo uso de memoria y ancho de banda
     * 
     * Integración con Lógica de Negocio:
     * - Base para validaciones en ParticipanteService
     * - Utilizado antes de permitir nuevas inscripciones
     * - Componente clave para UI responsiva
     * - Fundamento para reportes de participación
     * 
     * @param persona Persona cuya inscripción se desea verificar,
     *               puede ser null (retorna false automáticamente)
     * @param evento Evento específico donde verificar la inscripción,
     *              puede ser null (retorna false automáticamente)
     * 
     * @return true si existe una inscripción activa de la persona en el evento,
     *         false si no existe inscripción o algún parámetro es null
     * 
     * @implNote Operación de solo lectura sin efectos secundarios
     * @implNote La verificación por entidades completas aprovecha cache de JPA
     * @implNote COUNT es más eficiente que verificar existencia con LIMIT 1
     */
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
