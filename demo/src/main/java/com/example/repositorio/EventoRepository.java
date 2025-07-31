package com.example.repositorio;

import com.example.modelo.Evento;
import com.example.modelo.EstadoEvento;
import com.example.modelo.Persona;
import com.example.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * Repositorio especializado para operaciones de persistencia de eventos municipales.
 * 
 * Esta clase implementa el patrón Repository proporcionando una interfaz de acceso
 * a datos específicamente diseñada para entidades Evento y sus complejas relaciones.
 * Maneja la persistencia, consultas especializadas y optimizaciones de carga para
 * el dominio de eventos municipales con sus múltiples tipos y asociaciones.
 * 
 * Arquitectura de Persistencia:
 * - EntityManager per Operation: Cada método maneja su propio EntityManager
 * - Gestión manual de transacciones para control fino
 * - Estrategias múltiples de carga según caso de uso
 * - Optimizaciones específicas para prevenir N+1 queries
 * 
 * Estrategias de Carga de Relaciones:
 * - JOIN FETCH: Para carga eficiente en una consulta
 * - Lazy Loading Forzado: Para casos de carga selectiva
 * - Multi-Query Pattern: Para evitar producto cartesiano
 * - Type-Specific Loading: Carga según tipo de evento (Concierto, CicloDeCine)
 * 
 * Tipos de Consultas Implementadas:
 * - Consultas básicas sin relaciones (performance rápida)
 * - Consultas completas con todas las relaciones (datos completos)
 * - Consultas filtradas por estado (eventos disponibles)
 * - Consultas por responsable (vista personalizada)
 * - Búsquedas directas por ID (acceso individual)
 * - Consultas de validación (verificación de permisos)
 * 
 * Manejo de Herencia:
 * Implementa estrategias específicas para cargar relaciones polimórficas
 * de subclases como Concierto (artistas) y CicloDeCine (películas),
 * utilizando type checking e instanceof para carga diferenciada.
 * 
 * Operaciones CRUD Transaccionales:
 * - Create: Persist para entidades nuevas
 * - Read: Múltiples estrategias según necesidad
 * - Update: Merge para entidades existentes con detección automática
 * - Delete: Eliminación segura con manejo de contexto de persistencia
 * 
 * Gestión de Performance:
 * - Prevención de LazyInitializationException mediante carga anticipada
 * - Uso de DISTINCT para evitar duplicados en JOIN FETCH
 * - Separación de consultas para evitar producto cartesiano masivo
 * - Cierre inmediato de EntityManager para liberar recursos
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2024
 * 
 * @see com.example.modelo.Evento
 * @see com.example.modelo.Concierto
 * @see com.example.modelo.CicloDeCine
 * @see com.example.modelo.Persona
 * @see com.example.util.JpaUtil
 * @see EventoService
 */
public class EventoRepository {

    /**
     * Método helper que proporciona un EntityManager nuevo para operaciones de persistencia.
     * 
     * Implementa el patrón "EntityManager per Operation" donde cada operación
     * del repositorio obtiene su propio EntityManager fresco. Esta estrategia
     * evita problemas de concurrencia y asegura que cada operación tenga
     * un contexto de persistencia limpio e independiente.
     * 
     * Ventajas del Patrón:
     * - Aislamiento completo entre operaciones
     * - Sin problemas de concurrencia entre hilos
     * - Gestión explícita del ciclo de vida
     * - Contexto de persistencia predecible
     * 
     * @return Nueva instancia de EntityManager lista para usar
     * 
     * @implNote Cada EntityManager debe cerrarse explícitamente en el finally
     * @implNote Delegación a JpaUtil para configuración centralizada
     */
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    /**
     * Obtiene todos los eventos sin cargar sus relaciones asociadas.
     * 
     * Consulta básica optimizada para casos donde solo se necesitan
     * los datos principales de los eventos sin sus colecciones relacionadas.
     * Ideal para listados simples, conteos o cuando las relaciones
     * no son necesarias para el caso de uso específico.
     * 
     * Características de Performance:
     * - Consulta JPQL simple sin JOIN
     * - Sin carga de relaciones (lazy loading no activado)
     * - Mínimo overhead de red y memoria
     * - Ejecución rápida para grandes volúmenes
     * 
     * Casos de Uso Recomendados:
     * - Listados básicos de eventos
     * - Operaciones de conteo o estadísticas
     * - Exportación de datos básicos
     * - Validaciones que no requieren relaciones
     * 
     * @return Lista de eventos con solo datos básicos, puede estar vacía
     * 
     * @implNote Las relaciones estarán en estado lazy - no acceder sin EntityManager activo
     */
    public List<Evento> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Evento e", Evento.class).getResultList();
        } finally {
            em.close(); // Siempre cerrar el EntityManager
        }
    }

    /**
     * Obtiene todos los eventos con todas sus relaciones completamente cargadas.
     * 
     * Este método implementa una estrategia sofisticada de carga multi-consulta
     * para obtener eventos con todas sus relaciones sin crear un producto
     * cartesiano masivo. Utiliza el patrón de "múltiples consultas coordinadas"
     * para cargar eficientemente relaciones complejas y polimórficas.
     * 
     * Estrategia Multi-Query Implementada:
     * 
     * Consulta 1 - Eventos + Responsables:
     * "SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.responsables"
     * - Carga eventos base con sus responsables
     * - LEFT JOIN para incluir eventos sin responsables
     * - DISTINCT previene duplicados por múltiples responsables
     * 
     * Consulta 2 - Participantes:
     * "SELECT DISTINCT e FROM Evento e LEFT JOIN FETCH e.participantes WHERE e IN :eventos"
     * - Carga participantes para eventos ya obtenidos
     * - Usa lista de eventos como filtro para eficiencia
     * - Resultado se almacena en caché del EntityManager
     * 
     * Consulta 3 - Artistas (Conciertos):
     * "SELECT DISTINCT c FROM Concierto c LEFT JOIN FETCH c.artistas WHERE c.id IN :eventosIds"
     * - Específica para subclase Concierto
     * - Solo consulta eventos que son realmente conciertos
     * - Carga relación específica artistas
     * 
     * Consulta 4 - Películas (Ciclos de Cine):
     * "SELECT DISTINCT cc FROM CicloDeCine cc LEFT JOIN FETCH cc.peliculas WHERE cc.id IN :eventosIds"
     * - Específica para subclase CicloDeCine
     * - Solo consulta eventos que son ciclos de cine
     * - Carga relación específica películas
     * 
     * Ventajas de esta Estrategia:
     * - Evita producto cartesiano masivo que generaría una sola consulta
     * - Previene MultipleBagFetchException de Hibernate
     * - Control granular sobre qué relaciones cargar
     * - Mejor rendimiento con datasets grandes
     * - Flexibilidad para optimizar consultas individuales
     * 
     * Manejo de Cache y Performance:
     * - Cada consulta utiliza el mismo EntityManager
     * - Resultados se acumulan en el Persistence Context
     * - Las entidades ya cargadas se reutilizan automáticamente
     * - Stream API para extracción eficiente de IDs
     * 
     * Casos de Uso Ideales:
     * - Administración completa del sistema
     * - Reportes detallados con todas las relaciones
     * - Exportación completa de datos
     * - Vistas de gestión que requieren información completa
     * 
     * @return Lista de eventos con todas las relaciones cargadas completamente,
     *         puede estar vacía si no hay eventos en el sistema
     * 
     * @implNote Utiliza el mismo EntityManager para todas las consultas para
     *           aprovechar el cache de primer nivel
     * @implNote La estrategia multi-query es necesaria para evitar problemas
     *           de rendimiento con múltiples colecciones en una sola consulta
     */
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

    /**
     * Obtiene solo los eventos confirmados con todas sus relaciones cargadas.
     * 
     * Este método implementa un filtro específico para eventos en estado
     * CONFIRMADO, que son aquellos disponibles para inscripción pública.
     * Combina filtrado por estado con carga completa de relaciones usando
     * una estrategia híbrida de consulta inicial + lazy loading forzado.
     * 
     * Proceso de Carga Híbrida:
     * 
     * Fase 1 - Consulta Filtrada:
     * "SELECT e FROM Evento e WHERE e.estado = :estado"
     * - Filtra solo eventos en estado CONFIRMADO
     * - Consulta eficiente con índice en campo estado
     * - Obtiene solo eventos elegibles para inscripción
     * 
     * Fase 2 - Carga Forzada de Relaciones:
     * Para cada evento obtenido:
     * - evento.getResponsables().size() → Activa carga de responsables
     * - evento.getParticipantes().size() → Activa carga de participantes
     * - Carga específica según tipo de evento (instanceof)
     * 
     * Estrategia de Carga por Tipo:
     * - Concierto: Carga adicional de artistas
     * - CicloDeCine: Carga adicional de películas
     * - Otros tipos: Solo relaciones base
     * 
     * Ventajas del Enfoque Híbrido:
     * - Filtrado eficiente antes de cargar relaciones
     * - Solo se cargan relaciones de eventos relevantes
     * - Menor transferencia de datos innecesarios
     * - Control explícito sobre qué cargar
     * 
     * Prevención de LazyInitializationException:
     * - Todas las relaciones se activan antes de cerrar EntityManager
     * - Datos quedan completamente hidratados
     * - Seguro para uso posterior fuera del contexto de persistencia
     * 
     * Casos de Uso Específicos:
     * - Vista pública de eventos disponibles
     * - API de eventos para inscripción
     * - Listado de eventos activos en dashboard
     * - Consultas para usuarios finales
     * 
     * @return Lista de eventos confirmados con relaciones completas,
     *         puede estar vacía si no hay eventos confirmados
     * 
     * @implNote El lazy loading forzado se realiza dentro del contexto
     *           del EntityManager para evitar excepciones posteriores
     * @implNote Solo eventos en estado CONFIRMADO son considerados disponibles
     */
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

    /**
     * Obtiene eventos donde una persona específica figura como responsable.
     * 
     * Implementa consulta con JOIN directo para filtrar eventos basándose
     * en la relación many-to-many con responsables. Combina filtrado eficiente
     * con carga completa de relaciones para proporcionar vista personalizada
     * de eventos bajo la responsabilidad de una persona particular.
     * 
     * Consulta con JOIN Directo:
     * "SELECT e FROM Evento e JOIN e.responsables r WHERE r = :responsable"
     * - JOIN interno con colección responsables
     * - Filtro directo por entidad Persona
     * - Solo retorna eventos donde existe la relación
     * 
     * Estrategia de Carga Post-Consulta:
     * Aplica el mismo patrón de carga forzada que findAllDisponibles:
     * - Carga inmediata de responsables y participantes
     * - Carga diferenciada según tipo de evento
     * - Prevención proactiva de LazyInitializationException
     * 
     * Casos de Uso Principales:
     * - Vista "Mis Eventos" para responsables
     * - Dashboard personalizado por usuario
     * - Reportes de eventos por responsable
     * - Control de acceso basado en responsabilidad
     * - Auditoría de eventos por persona
     * 
     * Consideraciones de Seguridad:
     * - Filtrado automático por permisos de responsabilidad
     * - Solo eventos donde la persona tiene rol activo
     * - Base para implementar control de acceso granular
     * 
     * @param responsable Persona cuya responsabilidad se desea consultar,
     *                   debe ser una entidad persistida con ID válido
     * 
     * @return Lista de eventos donde la persona es responsable con relaciones completas,
     *         puede estar vacía si la persona no es responsable de ningún evento
     * 
     * @implNote Utiliza entidad completa como parámetro para aprovechar cache de JPA
     * @implNote El JOIN asegura que solo se retornen eventos con relación activa
     */
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

    /**
     * Busca un evento específico por ID con todas sus relaciones cargadas.
     * 
     * Implementa búsqueda directa por clave primaria utilizando el método
     * find() de EntityManager para máxima eficiencia. Incluye carga completa
     * de relaciones para proporcionar una entidad totalmente hidratada
     * lista para uso fuera del contexto de persistencia.
     * 
     * Estrategia de Búsqueda:
     * - EntityManager.find() para acceso directo por PK
     * - Búsqueda optimizada usando índice de clave primaria
     * - Verificación de existencia antes de cargar relaciones
     * - Retorno de null si la entidad no existe
     * 
     * Carga Condicional de Relaciones:
     * Solo si el evento existe:
     * - Activación de todas las relaciones base (responsables, participantes)
     * - Carga específica según tipo polimórfico (artistas o películas)
     * - Hidratación completa para uso posterior seguro
     * 
     * Casos de Uso Típicos:
     * - Carga de evento para edición en formularios
     * - Vista de detalles completos de evento
     * - Verificación de existencia con datos completos
     * - Preparación de entidad para operaciones complejas
     * 
     * Manejo de Casos Edge:
     * - ID inexistente: retorna null sin excepción
     * - Evento sin relaciones: carga exitosa con colecciones vacías
     * - Diferentes tipos de evento: carga adaptada automáticamente
     * 
     * @param id Identificador único del evento en la base de datos
     * 
     * @return Evento encontrado con todas las relaciones cargadas,
     *         o null si no existe un evento con el ID especificado
     * 
     * @implNote find() es más eficiente que JPQL para búsquedas por PK
     * @implNote La carga de relaciones es condicional para evitar NPE
     */
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

    /**
     * Persiste un evento nuevo o actualiza uno existente según su estado.
     * 
     * Implementa operación universal de guardado que detecta automáticamente
     * si debe crear una nueva entidad o actualizar una existente basándose
     * en el valor del ID. Maneja transacciones completas con rollback
     * automático en caso de error.
     * 
     * Lógica de Detección Automática:
     * - ID == 0: Entidad nueva → em.persist()
     * - ID > 0: Entidad existente → em.merge()
     * 
     * Operación Persist (Entidad Nueva):
     * - Hibernate genera ID automáticamente
     * - Se crean todas las relaciones asociadas
     * - La entidad pasa a estado MANAGED
     * - INSERT ejecutado en commit
     * 
     * Operación Merge (Entidad Existente):
     * - Hibernate detecta cambios automáticamente
     * - Solo campos modificados generan UPDATE
     * - Relaciones se sincronizan según configuración
     * - Entidad retornada queda en estado MANAGED
     * 
     * Gestión Transaccional:
     * - Transacción explícita para control de atomicidad
     * - Rollback automático en caso de cualquier excepción
     * - Commit solo si todas las operaciones son exitosas
     * - Liberación de recursos garantizada en finally
     * 
     * Casos de Uso:
     * - Guardado desde formularios de creación
     * - Actualización desde formularios de edición
     * - Operaciones de importación masiva
     * - Sincronización de datos externos
     * 
     * Manejo de Errores:
     * - Violaciones de constraints: rollback + excepción propagada
     * - Problemas de conectividad: rollback + excepción propagada
     * - Errores de validación: rollback + excepción propagada
     * 
     * @param evento Entidad a persistir o actualizar, no debe ser null
     * 
     * @throws Exception Si ocurre algún error durante la operación de guardado,
     *                  incluyendo violaciones de constraints o problemas de BD
     * 
     * @implNote La detección persist vs merge se basa únicamente en el valor del ID
     * @implNote Todas las relaciones configuradas se persisten automáticamente según cascade
     */
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

    /**
     * Actualiza un evento existente con nuevos datos.
     * 
     * Método especializado para actualizaciones que utiliza exclusivamente
     * merge() asumiendo que la entidad ya existe en la base de datos.
     * Proporciona una interfaz más semánticamente clara para operaciones
     * de actualización específicas.
     * 
     * Comportamiento de Merge:
     * - Sincroniza estado de entidad detached con BD
     * - Detecta automáticamente campos modificados
     * - Actualiza solo columnas que cambiaron (dirty checking)
     * - Maneja relaciones según configuración de cascade
     * 
     * Diferencias con save():
     * - Asume que entidad ya existe (no verifica ID)
     * - Siempre ejecuta merge(), nunca persist()
     * - Más explícito sobre la intención de actualizar
     * - Igual manejo transaccional y de errores
     * 
     * Casos de Uso Específicos:
     * - Actualizaciones desde formularios de edición
     * - Cambios de estado automáticos del sistema
     * - Sincronización de datos modificados externamente
     * - Operaciones batch de actualización
     * 
     * @param evento Evento con datos actualizados, debe tener ID válido existente
     * 
     * @throws Exception Si la entidad no existe, hay violaciones de constraints,
     *                  o cualquier otro error durante la actualización
     * 
     * @implNote Método equivalente a save() pero semánticamente más claro para updates
     * @implNote No verifica existencia previa - falla si ID no existe en BD
     */
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

    /**
     * Elimina un evento existente de la base de datos.
     * 
     * Implementa eliminación segura con manejo inteligente del contexto
     * de persistencia. Verifica si la entidad está adjunta al contexto
     * actual y la adjunta automáticamente si es necesario antes de
     * proceder con la eliminación.
     * 
     * Estrategia de Eliminación Segura:
     * 1. Verificación de contexto con em.contains()
     * 2. Si no está adjunta: merge() para adjuntarla
     * 3. Si ya está adjunta: usar directamente
     * 4. Ejecutar remove() sobre entidad adjunta
     * 5. Commit para hacer efectiva la eliminación
     * 
     * Manejo de Estado de Entidad:
     * - Entidad MANAGED: remove() directo
     * - Entidad DETACHED: merge() primero, luego remove()
     * - Entidad TRANSIENT: falla (no tiene ID para eliminar)
     * 
     * Consideraciones de Eliminación en Cascada:
     * - Relaciones configuradas con CASCADE.REMOVE se eliminan automáticamente
     * - Relaciones sin cascade pueden causar constraint violations
     * - Importante verificar configuración de cascade antes de eliminar
     * 
     * Casos de Uso:
     * - Cancelación definitiva de eventos
     * - Limpieza de datos de prueba
     * - Eliminación administrativa de eventos obsoletos
     * - Operaciones de mantenimiento de datos
     * 
     * Riesgos y Precauciones:
     * - Operación irreversible sin mecanismos de soft delete
     * - Puede afectar integridad referencial si hay datos dependientes
     * - Verificar permisos de eliminación antes de invocar
     * 
     * @param evento Evento a eliminar, debe tener ID válido existente
     * 
     * @throws Exception Si la entidad no existe, hay restricciones de FK,
     *                  o cualquier otro error durante la eliminación
     * 
     * @implNote merge() es necesario para entidades detached antes de remove()
     * @implNote La eliminación es física, no hay mecanismo de soft delete
     */
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

    /**
     * Verifica si una persona específica es responsable de un evento determinado.
     * 
     * Implementa consulta de verificación eficiente utilizando COUNT para
     * determinar la existencia de una relación de responsabilidad entre
     * una persona y un evento. Incluye validaciones de parámetros y
     * optimización mediante identificadores únicos.
     * 
     * Estrategia de Verificación:
     * - Validación preventiva de parámetros null
     * - Consulta COUNT optimizada para verificación de existencia
     * - Uso de identificadores únicos (DNI, ID) para máxima precisión
     * - Retorno booleano basado en conteo de coincidencias
     * 
     * Consulta de Verificación:
     * "SELECT COUNT(e) FROM Evento e JOIN e.responsables r 
     *  WHERE e.id = :eventoId AND r.dni = :personaDni"
     * 
     * Elementos Clave de la Consulta:
     * - COUNT(): Eficiente para verificación de existencia
     * - JOIN con responsables: Acceso directo a la relación
     * - Filtro por ID de evento: Identificación precisa del evento
     * - Filtro por DNI de persona: Identificación única de la persona
     * 
     * Ventajas del Enfoque:
     * - Máxima eficiencia: solo retorna un número, no entidades completas
     * - Precisión absoluta: utiliza identificadores únicos
     * - Sin overhead de carga de entidades innecesarias
     * - Validación robusta con manejo de casos null
     * 
     * Casos de Uso Principales:
     * - Control de acceso para operaciones de edición
     * - Validación de permisos antes de modificaciones
     * - Filtrado de eventos en interfaces de usuario
     * - Auditoría de responsabilidades
     * - Implementación de reglas de autorización
     * 
     * Validaciones Implementadas:
     * - Verificación de persona null → retorno inmediato false
     * - Verificación de evento null → retorno inmediato false
     * - Manejo seguro sin excepciones por parámetros inválidos
     * 
     * Consideraciones de Seguridad:
     * - Utiliza DNI como identificador único de persona
     * - No expone datos sensibles, solo confirmación booleana
     * - Base sólida para implementar control de acceso granular
     * 
     * @param persona Persona cuya responsabilidad se desea verificar,
     *               puede ser null (retorna false)
     * @param evento Evento sobre el cual se consulta la responsabilidad,
     *              puede ser null (retorna false)
     * 
     * @return true si la persona es responsable del evento específico,
     *         false si no existe la relación o algún parámetro es null
     * 
     * @implNote Utiliza COUNT para máxima eficiencia en verificación de existencia
     * @implNote La verificación por DNI asegura identificación única de persona
     * @implNote Operación de solo lectura sin efectos secundarios en el sistema
     */
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
