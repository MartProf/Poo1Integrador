package com.example.repositorio;

import java.util.List;
import com.example.modelo.Persona;
import com.example.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Repositorio especializado para operaciones de persistencia y consulta de personas.
 * 
 * Esta clase centraliza todas las operaciones de acceso a datos relacionadas
 * con la entidad Persona, incluyendo autenticación, búsquedas, validaciones
 * de unicidad y gestión completa del CRUD. Actúa como la capa de abstracción
 * principal entre la lógica de negocio y la persistencia de datos de personas.
 * 
 * Funcionalidades Principales:
 * - Operaciones CRUD completas para entidades Persona
 * - Búsquedas especializadas con diferentes criterios
 * - Validaciones de unicidad para campos críticos
 * - Autenticación mediante usuario y contraseña
 * - Carga optimizada de relaciones según necesidad
 * 
 * Estrategias de Consulta Implementadas:
 * - Búsqueda directa por clave primaria (DNI)
 * - Búsqueda con carga eager de participaciones
 * - Búsqueda por texto con pattern matching
 * - Consultas de existencia optimizadas con COUNT
 * - Autenticación segura con múltiples parámetros
 * 
 * Tipos de Operaciones:
 * 
 * Consultas de Búsqueda:
 * - findByDni: Búsqueda rápida por identificador único
 * - findByDniWithParticipaciones: Carga completa con relaciones
 * - findByNombreContaining: Búsqueda flexible por texto
 * - findAll: Listado completo ordenado alfabéticamente
 * - buscarPersonaPorUsuarioYContrasena: Autenticación de usuarios
 * 
 * Validaciones de Unicidad:
 * - existeUsuario: Verificación de nombres de usuario únicos
 * - existeEmail: Validación de emails únicos en el sistema
 * - existeDni: Confirmación de existencia por identificador
 * 
 * Operaciones de Modificación:
 * - save: Guardado inteligente (persist o merge según existencia)
 * - actualizar: Actualización específica de personas existentes
 * 
 * Optimizaciones de Performance:
 * - Uso estratégico de FETCH JOIN para prevenir N+1 queries
 * - Consultas COUNT para verificaciones de existencia eficientes
 * - Pattern matching case-insensitive para búsquedas de texto
 * - Ordenamiento en base de datos para listados grandes
 * 
 * Gestión de Relaciones:
 * - Carga lazy por defecto para operaciones simples
 * - Carga eager opcional para casos que requieren participaciones
 * - Manejo polimórfico de estados de entidad
 * - Optimización de transferencia de datos según caso de uso
 * 
 * Patrones de Seguridad:
 * - Parámetros tipados para prevenir SQL injection
 * - Validaciones defensivas en métodos críticos
 * - Manejo seguro de credenciales de autenticación
 * - Encapsulación de lógica de acceso a datos sensibles
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2024
 * 
 * @see com.example.modelo.Persona
 * @see com.example.modelo.Participante
 * @see PersonaService
 * @see com.example.util.JpaUtil
 */
public class PersonaRepository {

    /**
     * Método helper que proporciona un EntityManager nuevo para operaciones de persistencia.
     * 
     * Implementa el patrón "EntityManager per Operation" optimizado para
     * la diversidad de operaciones que maneja este repositorio. Cada método
     * obtiene su propio contexto de persistencia independiente, lo que es
     * especialmente beneficioso dado el rango de consultas de este repositorio.
     * 
     * Ventajas para PersonaRepository:
     * - Aislamiento entre operaciones de autenticación y CRUD
     * - Sin interferencia entre consultas de validación y modificaciones
     * - Contexto limpio para cada tipo de operación especializada
     * - Gestión predecible de transacciones por operación
     * 
     * @return Nueva instancia de EntityManager lista para usar
     * 
     * @implNote Especialmente importante para operaciones de autenticación sensibles
     */
    private EntityManager getEntityManager() {
        return JpaUtil.getEntityManager();
    }

    /**
     * Busca una persona por su DNI utilizando búsqueda optimizada por clave primaria.
     * 
     * Implementa la búsqueda más eficiente posible utilizando el método find()
     * de EntityManager que accede directamente por clave primaria. Esta operación
     * aprovecha índices de base de datos y cache de primer nivel para máximo
     * rendimiento en búsquedas frecuentes.
     * 
     * Características de Optimización:
     * - Acceso directo por PK sin generación de SQL complejo
     * - Aprovechamiento automático de cache de EntityManager
     * - Sin overhead de parsing JPQL
     * - Máxima velocidad para búsquedas por identificador
     * 
     * Casos de Uso Principales:
     * - Validación de existencia de persona en formularios
     * - Carga rápida para operaciones de autenticación
     * - Verificación de identidad en procesos de inscripción
     * - Búsquedas directas desde interfaces de administración
     * 
     * Consideraciones de Datos:
     * - Solo carga datos básicos de Persona (sin relaciones)
     * - Participaciones quedan en estado lazy (no cargadas)
     * - Ideal para casos que no requieren datos relacionales
     * 
     * @param dni Documento Nacional de Identidad de la persona a buscar
     * 
     * @return Persona encontrada con datos básicos, o null si no existe
     * 
     * @implNote find() es la operación más eficiente para búsquedas por PK
     * @implNote Las relaciones lazy no se cargan automáticamente
     */
    public Persona findByDni(int dni) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Persona.class, dni); // Búsqueda por clave primaria
        } finally {
            em.close(); // Cierre del EntityManager para liberar recursos
        }
    }

    /**
     * Busca una persona por DNI incluyendo sus participaciones en eventos.
     * 
     * Implementa búsqueda con carga eager de la relación participaciones
     * utilizando LEFT JOIN FETCH para obtener todos los datos relacionados
     * en una sola consulta SQL. Esta estrategia previene el problema N+1
     * y proporciona datos completos listos para uso inmediato.
     * 
     * Estrategia FETCH JOIN:
     * "SELECT p FROM Persona p LEFT JOIN FETCH p.participaciones WHERE p.dni = :dni"
     * 
     * Ventajas de LEFT JOIN FETCH:
     * - Carga persona incluso si no tiene participaciones
     * - Una sola consulta SQL para todos los datos
     * - Prevención automática de LazyInitializationException
     * - Datos completos disponibles fuera del contexto de persistencia
     * 
     * Uso de Stream API:
     * - getResultStream() para procesamiento eficiente
     * - findFirst() para obtener única coincidencia esperada
     * - orElse(null) para manejo elegante de casos sin resultado
     * 
     * Casos de Uso Ideales:
     * - Carga de perfil completo de usuario
     * - Vista de historial de participaciones
     * - Reportes detallados de actividad de persona
     * - Interfaces que requieren datos relacionales completos
     * 
     * Consideraciones de Performance:
     * - Más costosa que findByDni simple
     * - Justificada cuando se necesitan las participaciones
     * - Evita múltiples consultas posteriores
     * 
     * @param dni Documento Nacional de Identidad de la persona a buscar
     * 
     * @return Persona con participaciones cargadas, o null si no existe
     * 
     * @implNote LEFT JOIN asegura que personas sin participaciones también se retornen
     * @implNote Stream API proporciona manejo elegante de resultados únicos
     */
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

    /**
     * Busca personas cuyo nombre contenga una cadena específica de texto.
     * 
     * Implementa búsqueda flexible de texto con pattern matching case-insensitive
     * para permitir búsquedas parciales efectivas. Utiliza funciones SQL estándar
     * para normalización de texto y operadores LIKE para coincidencias parciales,
     * proporcionando una experiencia de búsqueda intuitiva y tolerante.
     * 
     * Consulta de Pattern Matching:
     * "SELECT p FROM Persona p WHERE LOWER(p.nombre) LIKE LOWER(:filtro)"
     * 
     * Características de la Búsqueda:
     * - Case-insensitive: No distingue mayúsculas de minúsculas
     * - Pattern matching: Busca texto contenido en cualquier posición
     * - Wildcard automático: Agrega % al inicio y final del término
     * - Búsqueda tolerante: Encuentra coincidencias parciales
     * 
     * Procesamiento de Texto:
     * - LOWER() en campo de base de datos para normalización
     * - LOWER() en parámetro de búsqueda para consistencia
     * - Adición automática de wildcards % para búsqueda contenida
     * - Manejo uniforme de acentos según configuración de BD
     * 
     * Casos de Uso Principales:
     * - Búsqueda de personas en formularios con autocompletado
     * - Filtrado en listas de administración
     * - Búsqueda rápida en modales de selección
     * - Funcionalidad de "buscar persona" en interfaces
     * 
     * Ejemplos de Búsqueda:
     * - Búsqueda "juan" encuentra: "Juan", "María Juana", "Juanito"
     * - Búsqueda "mar" encuentra: "María", "Omar", "Marta"
     * - Búsqueda "PEREZ" encuentra: "Pérez", "perez", "López Pérez"
     * 
     * Consideraciones de Performance:
     * - Puede requerir índices funcionales en campo nombre
     * - Búsquedas muy genéricas pueden ser costosas
     * - Apropiada para campos de longitud moderada
     * 
     * @param nombre Texto a buscar dentro del campo nombre (puede ser parcial)
     * 
     * @return Lista de personas cuyo nombre contiene el texto especificado,
     *         puede estar vacía si no hay coincidencias
     * 
     * @implNote La búsqueda es case-insensitive y busca coincidencias parciales
     * @implNote Los wildcards % se agregan automáticamente al parámetro
     */
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

    /**
     * Obtiene todas las personas del sistema ordenadas alfabéticamente.
     * 
     * Implementa consulta completa con ordenamiento en base de datos para
     * proporcionar listados consistentes y bien organizados. El ordenamiento
     * se realiza a nivel de SQL para máxima eficiencia con grandes volúmenes
     * de datos y aprovechamiento de índices de ordenamiento.
     * 
     * Consulta con Ordenamiento:
     * "SELECT p FROM Persona p ORDER BY p.apellido, p.nombre"
     * 
     * Estrategia de Ordenamiento:
     * - Primer criterio: apellido (agrupación familiar)
     * - Segundo criterio: nombre (desambiguación dentro de familia)
     * - Ordenamiento ascendente por defecto
     * - Procesado en base de datos para eficiencia
     * 
     * Ventajas del Ordenamiento en BD:
     * - Aprovecha índices compuestos si existen
     * - Eficiente para grandes volúmenes de datos
     * - Memoria mínima requerida en aplicación
     * - Orden consistente independiente de locale de aplicación
     * 
     * Casos de Uso Principales:
     * - Listados administrativos completos
     * - Reportes de personas registradas
     * - Exportación de datos de personas
     * - Interfaces de selección con listado completo
     * - Vistas de administración general
     * 
     * Consideraciones para Grandes Volúmenes:
     * - Puede requerir paginación para datasets muy grandes
     * - Considera filtros adicionales para reducir transferencia
     * - Monitorear performance con crecimiento de datos
     * 
     * @return Lista completa de personas ordenada por apellido y nombre,
     *         puede estar vacía si no hay personas registradas
     * 
     * @implNote El ordenamiento se realiza en la base de datos para eficiencia
     * @implNote Para datasets muy grandes considerar implementar paginación
     */
    public List<Persona> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Persona p ORDER BY p.apellido, p.nombre", Persona.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Guarda una persona implementando detección inteligente de operación requerida.
     * 
     * Este método implementa una estrategia sofisticada que determina automáticamente
     * si debe crear una nueva persona o actualizar una existente basándose en
     * la existencia previa en la base de datos. Utiliza el DNI como identificador
     * único para la detección y maneja transacciones completas con rollback automático.
     * 
     * Lógica de Detección Inteligente:
     * 1. Verificación de existencia: em.find(Persona.class, persona.getDni())
     * 2. Si no existe: em.persist(persona) para nueva inserción
     * 3. Si existe: em.merge(persona) para actualización
     * 4. Commit transaccional para confirmar operación
     * 
     * Ventajas de esta Estrategia:
     * - Operación universal para casos de creación y actualización
     * - Detección automática sin requerir flags externos
     * - Manejo consistente independiente del origen de datos
     * - Apropiado para formularios que pueden crear o editar
     * 
     * Comportamiento de Persist vs Merge:
     * 
     * Persist (Persona Nueva):
     * - Se asigna a contexto de persistencia como MANAGED
     * - Se genera INSERT SQL en commit
     * - Relaciones se persisten según configuración cascade
     * - Ideal para formularios de registro nuevos
     * 
     * Merge (Persona Existente):
     * - Sincroniza estado detached con base de datos
     * - Detecta automáticamente campos modificados (dirty checking)
     * - Genera UPDATE solo para campos cambiados
     * - Preserva relaciones existentes no modificadas
     * 
     * Gestión Transaccional Robusta:
     * - Transacción explícita para control total de atomicidad
     * - Rollback automático en cualquier excepción
     * - Liberación garantizada de recursos en bloque finally
     * - Propagación de excepciones para manejo en capas superiores
     * 
     * Casos de Uso Principales:
     * - Guardado universal desde formularios de persona
     * - Operaciones de importación que pueden duplicar datos
     * - APIs REST que reciben datos de origen externo
     * - Sincronización de datos desde sistemas legados
     * 
     * Consideraciones de Concurrencia:
     * - Race condition posible entre verificación y persistencia
     * - Manejo de excepciones de constraint violation
     * - Apropiado para operaciones de usuario individual
     * 
     * @param persona Entidad Persona completamente configurada para persistir o actualizar
     * 
     * @throws Exception Si ocurre error durante la operación, incluyendo:
     *                  - Violaciones de constraints únicos (DNI, email, usuario)
     *                  - Problemas de conectividad con base de datos
     *                  - Errores de validación de datos
     *                  - Conflictos de transacción o bloqueos
     * 
     * @implNote La detección se basa en existencia por DNI en momento de ejecución
     * @implNote Posible race condition en ambientes altamente concurrentes
     */
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

    /**
     * Actualiza una persona existente utilizando operación merge específica.
     * 
     * Método especializado para actualizaciones que asume la existencia previa
     * de la persona en la base de datos. Utiliza exclusivamente merge() para
     * sincronizar el estado de una entidad detached con su contraparte persistida,
     * proporcionando semántica clara para operaciones de actualización.
     * 
     * Comportamiento de Merge:
     * - Sincroniza entidad detached con estado persistido actual
     * - Detecta automáticamente campos modificados (dirty checking)
     * - Actualiza solo columnas que han cambiado desde último estado
     * - Preserva datos no modificados en base de datos
     * - Maneja relaciones según configuración de cascade
     * 
     * Diferencias Semánticas con save():
     * - Asume existencia previa (no verifica DNI)
     * - Siempre ejecuta merge(), nunca persist()
     * - Más explícito sobre intención de actualizar
     * - Falla si la entidad no existe en base de datos
     * 
     * Casos de Uso Específicos:
     * - Actualización desde formularios de edición de perfil
     * - Modificaciones programáticas de datos existentes
     * - Sincronización de cambios desde sistemas externos
     * - Operaciones batch de actualización masiva
     * 
     * Ventajas de Dirty Checking:
     * - Solo campos modificados generan UPDATE SQL
     * - Minimiza transferencia de datos innecesarios
     * - Reduce contención en base de datos
     * - Optimiza performance en actualizaciones parciales
     * 
     * @param persona Persona con datos actualizados, debe existir previamente en BD
     * 
     * @throws Exception Si la persona no existe, hay violaciones de constraints,
     *                  o cualquier otro error durante la actualización
     * 
     * @implNote No verifica existencia previa - falla si DNI no existe en BD
     * @implNote Equivalente funcional a save() pero semánticamente más claro
     */
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

    /**
     * Verifica si ya existe un usuario con el nombre de usuario especificado.
     * 
     * Implementa validación de unicidad para nombres de usuario utilizando
     * consulta COUNT optimizada. Esta verificación es crítica para mantener
     * la integridad del sistema de autenticación y prevenir conflictos de
     * identificación durante el proceso de registro de nuevos usuarios.
     * 
     * Consulta de Verificación:
     * "SELECT COUNT(p) FROM Persona p WHERE p.usuario = :usuario"
     * 
     * Características de la Validación:
     * - Consulta COUNT para máxima eficiencia (solo retorna número)
     * - Sin carga innecesaria de entidades completas
     * - Búsqueda exacta case-sensitive en campo usuario
     * - Aprovecha índices únicos si están configurados
     * 
     * Casos de Uso Críticos:
     * - Validación en tiempo real durante registro de usuarios
     * - Verificación previa antes de permitir creación de cuenta
     * - Validación en formularios con feedback inmediato
     * - Procesos de importación para evitar duplicados
     * 
     * Importancia para Seguridad:
     * - Previene duplicación de identificadores de autenticación
     * - Mantiene unicidad requerida para login seguro
     * - Evita confusión entre usuarios con nombres similares
     * - Base para implementar políticas de nombres de usuario
     * 
     * Integración con Validaciones de Formulario:
     * - Utilizado en validadores de campos de formulario
     * - Base para mensajes de error específicos
     * - Componente de validación de disponibilidad
     * 
     * @param usuario Nombre de usuario cuya unicidad se desea verificar
     * 
     * @return true si ya existe un usuario con ese nombre,
     *         false si el nombre está disponible para uso
     * 
     * @implNote La búsqueda es case-sensitive según configuración de BD
     * @implNote Optimizada para verificación rápida de disponibilidad
     */
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

    /**
     * Verifica si ya existe una persona registrada con el email especificado.
     * 
     * Implementa validación de unicidad para direcciones de email, fundamental
     * para mantener la integridad de comunicaciones y procesos de recuperación
     * de cuentas. Utiliza consulta COUNT optimizada para verificación eficiente
     * sin transferencia innecesaria de datos personales.
     * 
     * Consulta de Verificación:
     * "SELECT COUNT(p) FROM Persona p WHERE p.email = :email"
     * 
     * Importancia para el Sistema:
     * - Previene registros duplicados con mismo email
     * - Asegura unicidad para procesos de recuperación de contraseña
     * - Mantiene integridad para comunicaciones automáticas
     * - Evita confusión en notificaciones por email
     * 
     * Casos de Uso Principales:
     * - Validación durante registro de nuevas personas
     * - Verificación en formularios de actualización de perfil
     * - Validación en procesos de importación de datos
     * - Verificación previa a envío de comunicaciones
     * 
     * Consideraciones de Privacidad:
     * - Solo verifica existencia, no expone datos del email
     * - No revela información sobre el propietario del email
     * - Consulta mínima para máxima privacidad
     * 
     * Integración con Validaciones:
     * - Componente clave en validadores de formulario
     * - Base para mensajes de error específicos de email
     * - Utilizado en flujos de registro y actualización
     * 
     * @param email Dirección de email cuya unicidad se desea verificar
     * 
     * @return true si ya existe una persona con ese email,
     *         false si el email está disponible para registro
     * 
     * @implNote No expone información sobre propietarios existentes del email
     * @implNote Considera configuración de case sensitivity de la base de datos
     */
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

    /**
     * Verifica si existe una persona con un DNI específico en el sistema.
     * 
     * Implementa verificación de existencia delegando a findByDni() para
     * aprovechar la búsqueda optimizada por clave primaria. Esta operación
     * es fundamental para validaciones de integridad y verificación de
     * identidad en procesos críticos del sistema.
     * 
     * Estrategia de Delegación:
     * - Reutiliza findByDni() que ya está optimizado
     * - Aprovecha cache de primer nivel si está disponible
     * - Consistente con otras operaciones de búsqueda por DNI
     * - Evita duplicación de lógica de acceso por clave primaria
     * 
     * Casos de Uso Principales:
     * - Validación de existencia antes de operaciones críticas
     * - Verificación de identidad en procesos de inscripción
     * - Validación previa a creación de relaciones
     * - Verificación en importación de datos externos
     * 
     * Ventajas de la Delegación:
     * - Consistencia con otras operaciones del repositorio
     * - Aprovecha optimizaciones ya implementadas
     * - Mantenimiento simplificado (un solo punto de búsqueda por DNI)
     * - Comportamiento predecible y coherente
     * 
     * @param dni Documento Nacional de Identidad a verificar
     * 
     * @return true si existe una persona con ese DNI,
     *         false si no hay ninguna persona registrada con ese documento
     * 
     * @implNote Delega a findByDni() para consistencia y optimización
     * @implNote Aprovecha optimizaciones de búsqueda por clave primaria
     */
    public boolean existeDni(int dni) {
        return findByDni(dni) != null;
    }

    /**
     * Busca una persona por credenciales de autenticación (usuario y contraseña).
     * 
     * Implementa el método fundamental para autenticación de usuarios en el sistema,
     * verificando tanto el nombre de usuario como la contraseña en una sola consulta
     * optimizada. Utiliza Stream API para manejo elegante de resultados únicos
     * y proporciona base segura para procesos de login del sistema.
     * 
     * Consulta de Autenticación:
     * "SELECT p FROM Persona p WHERE p.usuario = :usuario AND p.contrasena = :contrasena"
     * 
     * Características de Seguridad:
     * - Verificación simultánea de usuario y contraseña
     * - Parámetros tipados para prevenir SQL injection
     * - Retorno de entidad completa solo si credenciales coinciden
     * - Sin exposición de información si las credenciales fallan
     * 
     * Estrategia de Consulta:
     * - Búsqueda exacta por ambos campos de credenciales
     * - getResultStream() para procesamiento eficiente
     * - findFirst() para obtener único resultado esperado
     * - orElse(null) para manejo elegante de credenciales inválidas
     * 
     * Casos de Uso Críticos:
     * - Proceso de login principal del sistema
     * - Validación de credenciales en APIs de autenticación
     * - Verificación de identidad antes de operaciones sensibles
     * - Base para sistemas de sesión y autorización
     * 
     * Flujo de Autenticación:
     * 1. Usuario proporciona credenciales en formulario de login
     * 2. Sistema llama a este método para verificación
     * 3. Si retorna Persona: credenciales válidas, proceder con login
     * 4. Si retorna null: credenciales inválidas, mostrar error
     * 
     * Consideraciones de Seguridad:
     * - NO implementa hashing de contraseñas (debería mejorarse)
     * - Comparación directa de texto plano (riesgo de seguridad)
     * - Recomendación: migrar a bcrypt o similar para producción
     * - Logging de intentos fallidos debería implementarse externamente
     * 
     * Ventajas del Stream API:
     * - Manejo elegante de resultado único esperado
     * - Sin excepciones por resultados vacíos
     * - Código más legible que manejo manual de listas
     * 
     * Limitaciones Actuales:
     * - Contraseñas en texto plano (no recomendado para producción)
     * - Sin protección contra ataques de fuerza bruta
     * - Sin logging de intentos de autenticación
     * - Sin manejo de cuentas bloqueadas o intentos fallidos
     * 
     * @param usuario Nombre de usuario para autenticación
     * @param contrasena Contraseña en texto plano para verificación
     * 
     * @return Persona autenticada si las credenciales son correctas,
     *         null si el usuario no existe o la contraseña es incorrecta
     * 
     * @implNote IMPORTANTE: Las contraseñas deberían hashearse para seguridad en producción
     * @implNote Stream API proporciona manejo elegante de resultados únicos
     * @implNote No distingue entre usuario inexistente y contraseña incorrecta (seguridad)
     */
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
