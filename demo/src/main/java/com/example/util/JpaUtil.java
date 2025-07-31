package com.example.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Clase utilitaria para la gestión centralizada de JPA en el sistema municipal.
 * 
 * Esta clase implementa el patrón Singleton para garantizar una única instancia
 * del EntityManagerFactory durante toda la vida de la aplicación, proporcionando
 * un punto de acceso unificado para todas las operaciones de persistencia.
 * 
 * Responsabilidades Principales:
 * - Inicialización única del EntityManagerFactory usando persistence.xml
 * - Gestión del ciclo de vida de la factory de EntityManager
 * - Provisión de EntityManager nuevos bajo demanda
 * - Cierre controlado de recursos JPA al finalizar la aplicación
 * - Manejo robusto de errores durante la inicialización
 * 
 * Arquitectura de Inicialización:
 * La clase utiliza un bloque estático para garantizar que la inicialización
 * del EntityManagerFactory ocurra una sola vez cuando la clase se carga por
 * primera vez. Este enfoque asegura que todos los componentes que necesiten
 * acceso a JPA compartan la misma instancia de factory.
 * 
 * Configuración de Persistencia:
 * Se basa completamente en el archivo persistence.xml ubicado en META-INF,
 * donde se define la unidad de persistencia "Municipalidad" con todas las
 * configuraciones específicas de la base de datos, entidades mapeadas,
 * propiedades de conexión y configuraciones de Hibernate.
 * 
 * Patrón de Uso Recomendado:
 * - Repositorios obtienen la factory para crear EntityManager según necesidad
 * - Servicios pueden obtener EntityManager directos para operaciones simples
 * - Cada operación debe manejar apropiadamente el ciclo de vida del EntityManager
 * - Siempre cerrar EntityManager después de uso para evitar memory leaks
 * 
 * Estrategia de Manejo de Errores:
 * Cualquier error durante la inicialización resulta en ExceptionInInitializerError,
 * lo que efectivamente impide el uso de JPA en la aplicación y fuerza una
 * terminación controlada, evitando estados inconsistentes.
 * 
 * Consideraciones de Threading:
 * EntityManagerFactory es thread-safe y puede ser compartida entre múltiples
 * hilos. Sin embargo, los EntityManager individuales NO son thread-safe y
 * deben ser usados por un solo hilo a la vez.
 * 
 * Gestión de Recursos:
 * Es crucial llamar al método close() durante el shutdown de la aplicación
 * para liberar apropiadamente todas las conexiones de base de datos y
 * recursos asociados con JPA.
 */
public class JpaUtil {

    /**
     * Factory única de EntityManager para toda la aplicación.
     * 
     * Esta instancia estática final garantiza que solo exista una factory
     * de EntityManager durante toda la ejecución de la aplicación, implementando
     * efectivamente el patrón Singleton para la gestión de persistencia JPA.
     * 
     * La factory se inicializa una única vez en el bloque estático y permanece
     * inmutable durante toda la vida de la aplicación, proporcionando acceso
     * thread-safe a la creación de EntityManager para todas las operaciones
     * de persistencia del sistema municipal.
     */
    private static final EntityManagerFactory emf;

    /**
     * Bloque de inicialización estática para configuración única de JPA.
     * 
     * Este bloque se ejecuta una sola vez cuando la clase JpaUtil es cargada
     * por primera vez por el ClassLoader, garantizando inicialización única
     * y thread-safe del EntityManagerFactory.
     * 
     * Proceso de Inicialización:
     * 1. Lectura automática del archivo persistence.xml desde META-INF
     * 2. Búsqueda de la unidad de persistencia "Municipalidad"
     * 3. Configuración de todas las entidades mapeadas
     * 4. Establecimiento de conexión con la base de datos
     * 5. Inicialización de pools de conexión según configuración
     * 
     * Manejo Robusto de Errores:
     * Cualquier fallo durante la inicialización (archivo persistence.xml
     * inexistente, configuración inválida, problemas de conexión DB) resulta
     * en ExceptionInInitializerError que previene el uso de JPA e indica
     * claramente la necesidad de revisar la configuración.
     * 
     * Configuraciones Cargadas:
     * - Propiedades de conexión a base de datos (URL, usuario, password)
     * - Configuraciones específicas de Hibernate (dialecto, DDL, logging)
     * - Mapeo de todas las entidades del modelo municipal
     * - Configuraciones de cache y optimización
     */
    static {
        try {
            // Acá se lee tu persistence.xml
            emf = Persistence.createEntityManagerFactory("Municipalidad");
        } catch (Throwable ex) {
            System.err.println("Inicialización de EntityManagerFactory falló: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Proporciona acceso a la factory global de EntityManager.
     * 
     * Este método es el punto de acceso principal para obtener la instancia
     * única de EntityManagerFactory que gestiona toda la persistencia JPA
     * en el sistema municipal. Es utilizado principalmente por las clases
     * Repository que necesitan crear EntityManager según demanda.
     * 
     * Características de la Factory Retornada:
     * - Thread-safe: puede ser utilizada concurrentemente por múltiples hilos
     * - Singleton: siempre retorna la misma instancia durante toda la app
     * - Configurada: completamente inicializada con todas las entidades
     * - Conectada: con pool de conexiones activo a la base de datos
     * 
     * Patrón de Uso en Repositorios:
     * Los repositorios típicamente almacenan esta factory como campo final
     * y la utilizan para crear EntityManager nuevos en cada operación:
     * 
     * private final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();
     * 
     * public void operacion() {
     *     EntityManager em = emf.createEntityManager();
     *     try {
     *         // operaciones JPA
     *     } finally {
     *         em.close();
     *     }
     * }
     * 
     * Ventajas del Acceso Centralizado:
     * - Configuración única y consistente en toda la aplicación
     * - Facilita testing mediante mocking de la factory
     * - Simplifica el cambio de configuración JPA en un solo punto
     * - Garantiza uso eficiente de recursos de conexión
     * 
     * @return La instancia única de EntityManagerFactory configurada para
     *         la unidad de persistencia "Municipalidad"
     * 
     * @implNote La factory retornada nunca es null debido a la inicialización
     *           en bloque estático que falla rápido si hay problemas
     */
    // Devuelve el EMF global
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    /**
     * Crea y retorna un nuevo EntityManager listo para uso inmediato.
     * 
     * Este método de conveniencia simplifica la obtención de EntityManager
     * para casos donde se necesita acceso directo sin gestionar la factory.
     * Es especialmente útil en servicios o componentes que realizan operaciones
     * JPA ocasionales y prefieren una API más simple.
     * 
     * Características del EntityManager Retornado:
     * - Nuevo: cada llamada crea una instancia completamente nueva
     * - No-transaccional: sin transacción activa, debe iniciarse manualmente
     * - Thread-unsafe: debe ser usado por un único hilo
     * - Requiere cierre: es responsabilidad del llamador cerrarlo
     * 
     * Patrón de Uso Recomendado:
     * 
     * EntityManager em = JpaUtil.getEntityManager();
     * try {
     *     em.getTransaction().begin();
     *     // operaciones JPA
     *     em.getTransaction().commit();
     * } catch (Exception e) {
     *     if (em.getTransaction().isActive()) {
     *         em.getTransaction().rollback();
     *     }
     *     throw e;
     * } finally {
     *     em.close(); // CRÍTICO: siempre cerrar
     * }
     * 
     * Casos de Uso Apropiados:
     * - Operaciones simples de consulta o persistencia
     * - Servicios que no usan el patrón Repository
     * - Operaciones one-shot que no requieren reutilización
     * - Testing y desarrollo rápido
     * 
     * Consideraciones de Performance:
     * - Crear EntityManager tiene overhead mínimo pero no despreciable
     * - Para operaciones múltiples, considerar reutilizar el mismo EM
     * - El pool de conexiones subyacente optimiza el acceso a DB
     * 
     * Gestión de Memoria Crítica:
     * Es absolutamente esencial cerrar cada EntityManager obtenido através
     * de este método. Fallar en cerrarlos resulta en memory leaks y
     * agotamiento del pool de conexiones a base de datos.
     * 
     * @return Un nuevo EntityManager configurado y conectado, listo para
     *         realizar operaciones de persistencia. Nunca retorna null.
     * 
     * @implNote Equivale a getEntityManagerFactory().createEntityManager()
     * @implNote Cada EntityManager tiene su propio contexto de persistencia
     */
    // Opcional: devuelve un EM nuevo (útil si no usás repositorio con EMF directo)
    public static jakarta.persistence.EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Cierra ordenadamente la factory de EntityManager y libera todos los recursos JPA.
     * 
     * Este método es crucial para el shutdown limpio de la aplicación, asegurando
     * que todas las conexiones de base de datos, pools de conexión, caches y
     * otros recursos asociados con JPA sean liberados apropiadamente antes de
     * que la aplicación termine.
     * 
     * Proceso de Cierre Controlado:
     * 1. Verificación de estado abierto de la factory
     * 2. Cierre de todos los EntityManager activos (si los hay)
     * 3. Liberación del pool de conexiones de base de datos
     * 4. Limpieza de caches de segundo nivel
     * 5. Liberación de recursos de metadatos de entidades
     * 6. Notificación a proveedores JPA para cleanup final
     * 
     * Importancia del Cierre Ordenado:
     * - Previene memory leaks en la aplicación
     * - Evita warnings de connections no cerradas
     * - Permite que la base de datos libere locks y recursos
     * - Facilita restart limpio de la aplicación en desarrollo
     * - Cumple con mejores prácticas de gestión de recursos
     * 
     * Momento Apropiado de Llamada:
     * - Durante shutdown hooks de la aplicación
     * - En bloques finally de aplicaciones de corta duración
     * - Al cerrar la ventana principal en aplicaciones JavaFX/Swing
     * - En métodos @PreDestroy de contenedores Enterprise
     * 
     * Integración con Lifecycle de Aplicación:
     * 
     * // En JavaFX Application
     * @Override
     * public void stop() throws Exception {
     *     JpaUtil.close();
     *     super.stop();
     * }
     * 
     * // Shutdown Hook
     * Runtime.getRuntime().addShutdownHook(new Thread(JpaUtil::close));
     * 
     * Comportamiento Seguro:
     * El método verifica el estado de la factory antes de intentar cerrarla,
     * evitando IllegalStateException si ya fue cerrada previamente. Es seguro
     * llamar este método múltiples veces.
     * 
     * Efectos Post-Cierre:
     * Después de llamar este método, cualquier intento de usar getEntityManager()
     * o getEntityManagerFactory() resultará en IllegalStateException, ya que
     * la factory habrá sido cerrada y no puede crear nuevos EntityManager.
     * 
     * @implNote Método idempotente: múltiples llamadas son seguras
     * @implNote Después del cierre, la factory no puede ser reabierta
     */
    // Cierra todo (se llama al cerrar la app)
    public static void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
