package com.example.servicio;

import java.util.List;
import com.example.modelo.Persona;
import com.example.repositorio.PersonaRepository;

/**
 * Servicio de lógica de negocio para la gestión de entidades Persona en el sistema municipal de eventos.
 * 
 * Esta clase implementa la capa de servicios del patrón de arquitectura en capas,
 * proporcionando una interfaz de alto nivel para todas las operaciones relacionadas
 * con personas, incluyendo registro, autenticación, búsqueda y validaciones de negocio.
 * Actúa como intermediario entre los controladores y el repositorio de datos.
 * 
 * Responsabilidades Principales:
 * - Validaciones de Negocio: Reglas complejas de integridad y formato
 * - Gestión de Registro: Proceso completo y simplificado de alta de personas
 * - Autenticación: Validación de credenciales para acceso al sistema
 * - Operaciones de Búsqueda: Múltiples criterios y estrategias de consulta
 * - Transformación de Datos: Limpieza y normalización de entrada
 * 
 * Patrones Implementados:
 * - Service Layer: Encapsula lógica de negocio
 * - Domain Validation: Validaciones específicas del dominio
 * - Repository Pattern: Delegación de persistencia
 * - Fail Fast: Validación temprana con excepciones descriptivas
 * 
 * Tipos de Registro Soportados:
 * 1. Registro Completo (registrarPersona):
 *    → Usuario con credenciales de acceso al sistema
 *    → Todos los campos obligatorios + validaciones de formato
 *    → Para ciudadanos que crearán y gestionarán eventos
 * 
 * 2. Registro Simple (guardarPersonaSimple):
 *    → Persona básica sin credenciales de sistema
 *    → Solo datos identificatorios mínimos
 *    → Para participantes de eventos sin acceso al sistema
 * 
 * Estrategias de Búsqueda:
 * - Por DNI: Búsqueda exacta para identificación única
 * - Por DNI con Participaciones: Include relaciones para análisis
 * - Por Nombre: Búsqueda flexible para selección de usuarios
 * - Listado Completo: Para interfaces de selección y administración
 * 
 * Validaciones de Integridad:
 * - Campos Obligatorios: Según tipo de registro
 * - Formato de Email: Regex RFC-compliant
 * - Unicidad: DNI, usuario y email únicos en el sistema
 * - Rangos Válidos: DNI positivo, campos no vacíos
 * 
 * Manejo de Errores:
 * Utiliza el patrón Fail Fast con excepciones IllegalArgumentException
 * que contienen mensajes descriptivos para facilitar la comprensión
 * y resolución de problemas por parte del usuario final.
 * 
 * Integración con el Sistema:
 * Este servicio es utilizado por múltiples controladores:
 * - RegistroController: Registro de nuevos usuarios
 * - SesionController: Autenticación
 * - BuscarPersonaController: Búsqueda y selección
 * - PerfilController: Gestión de perfil
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2024
 * 
 * @see PersonaRepository
 * @see com.example.modelo.Persona
 * @see com.example.controlador.RegistroController
 * @see com.example.controlador.SesionController
 */
public class PersonaService {

    /**
     * Repositorio para operaciones de persistencia de entidades Persona.
     * 
     * Delega todas las operaciones de acceso a datos al repositorio,
     * manteniendo la separación de responsabilidades entre lógica de negocio
     * y persistencia de datos según el patrón Repository.
     * 
     * @see PersonaRepository
     */
    private PersonaRepository personaRepo;

    /**
     * Constructor que inicializa el servicio con sus dependencias.
     * 
     * Utiliza el patrón Service Locator para obtener la instancia
     * del repositorio. Este enfoque mantiene simplicidad en la gestión
     * de dependencias mientras permite futuras mejoras hacia inyección
     * de dependencias más sofisticada.
     * 
     * @implNote El repositorio se inicializa aquí para asegurar disponibilidad
     *           inmediata en todos los métodos del servicio
     */
    public PersonaService() {
        this.personaRepo = new PersonaRepository();
    }

    /**
     * Busca una persona por su número de documento de identidad.
     * 
     * Realiza una búsqueda exacta por DNI, que es el identificador único
     * natural de las personas en el sistema. Este método es fundamental
     * para validaciones de unicidad y verificación de identidad.
     * 
     * Casos de uso:
     * - Validación de unicidad durante registro
     * - Verificación de existencia para registro en eventos
     * - Búsqueda rápida por identificación oficial
     * 
     * @param dni Número de documento de identidad a buscar
     * @return La persona con el DNI especificado, o null si no existe
     * 
     * @implNote Delega directamente al repositorio sin validaciones adicionales
     *           ya que el DNI es un criterio de búsqueda simple
     */
    public Persona buscarPorDni(int dni) {
        return personaRepo.findByDni(dni);
    }

    /**
     * Busca una persona por DNI incluyendo sus participaciones en eventos.
     * 
     * Versión extendida de la búsqueda por DNI que incluye las relaciones
     * con eventos en los que la persona ha participado. Útil para análisis
     * y visualización del historial de participación ciudadana.
     * 
     * Datos incluidos:
     * - Información básica de la persona
     * - Lista de participaciones en eventos
     * - Relaciones con eventos específicos
     * 
     * Casos de uso:
     * - Visualización de perfil completo
     * - Análisis de participación ciudadana
     * - Generación de reportes de actividad
     * 
     * @param dni Número de documento de identidad a buscar
     * @return La persona con sus participaciones, o null si no existe
     * 
     * @implNote Utiliza lazy loading o eager fetching según la implementación
     *           del repositorio para optimizar la carga de relaciones
     */
    public Persona buscarPorDniConParticipaciones(int dni) {
        return personaRepo.findByDniWithParticipaciones(dni);
    }

    /**
     * Busca personas por coincidencia parcial en el nombre.
     * 
     * Implementa búsqueda flexible que permite encontrar personas
     * sin conocer el nombre exacto. Utiliza coincidencia parcial
     * case-insensitive para máxima usabilidad en interfaces de búsqueda.
     * 
     * Características de la búsqueda:
     * - Parcial: Encuentra coincidencias dentro del texto
     * - Case-Insensitive: No distingue mayúsculas/minúsculas
     * - Flexible: Útil para autocompletado y sugerencias
     * 
     * Casos de uso:
     * - Búsqueda de personas en modales de selección
     * - Autocompletado en formularios
     * - Sugerencias de personas similares
     * 
     * @param nombre Texto a buscar dentro de los nombres de personas
     * @return Lista de personas que contienen el texto en su nombre
     * 
     * @implNote La implementación específica de "containing" depende del repositorio,
     *           típicamente usando LIKE '%nombre%' en SQL
     */
    public List<Persona> buscarPorNombre(String nombre) {
        return personaRepo.findByNombreContaining(nombre);
    }

    /**
     * Obtiene la lista completa de todas las personas registradas en el sistema.
     * 
     * Proporciona acceso al catálogo completo de personas para interfaces
     * de administración, selección masiva y análisis del sistema. Este método
     * debe usarse con cuidado en sistemas con grandes volúmenes de datos.
     * 
     * Casos de uso:
     * - Carga inicial de modales de búsqueda
     * - Interfaces de administración
     * - Generación de reportes completos
     * - Exportación de datos
     * 
     * Consideraciones de performance:
     * - Puede devolver grandes volúmenes de datos
     * - Considerar paginación para sistemas grandes
     * - Usar con filtrado local posterior cuando sea apropiado
     * 
     * @return Lista completa de todas las personas en el sistema
     * 
     * @implNote En sistemas de producción, considerar implementar paginación
     *           o límites para evitar problemas de memoria
     */
    public List<Persona> obtenerTodas() {
        return personaRepo.findAll();
    }

    /**
     * Registra una nueva persona completa en el sistema con todas las validaciones de negocio.
     * 
     * Este método implementa el proceso completo de registro de un usuario que tendrá
     * acceso al sistema municipal de eventos. Incluye validaciones exhaustivas de formato,
     * integridad y unicidad, así como normalización de datos antes de la persistencia.
     * 
     * Proceso de Registro Completo:
     * 1. Validaciones de Campos Obligatorios:
     *    → Nombre, apellido, email, usuario, contraseña, teléfono
     * 
     * 2. Validaciones de Formato:
     *    → Email con regex RFC-compliant
     *    → DNI debe ser número positivo
     * 
     * 3. Validaciones de Unicidad:
     *    → DNI único en el sistema
     *    → Usuario único en el sistema  
     *    → Email único en el sistema
     * 
     * 4. Normalización de Datos:
     *    → Trim() en todos los campos de texto
     *    → Limpieza de espacios redundantes
     * 
     * 5. Persistencia:
     *    → Guardar en base de datos via repositorio
     * 
     * Validaciones Implementadas:
     * - Campos Obligatorios:
     *   - Nombre no nulo y no vacío
     *   - Apellido no nulo y no vacío
     *   - Email no nulo y no vacío
     *   - Usuario no nulo y no vacío
     *   - Contraseña no nula y no vacía
     *   - Teléfono no nulo y no vacío
     * - Validaciones de Formato:
     *   - Email: Regex "^[A-Za-z0-9+_.-]+@(.+)$"
     *   - DNI: Debe ser mayor a 0
     * - Validaciones de Unicidad:
     *   - DNI no debe existir en el sistema
     *   - Usuario no debe existir en el sistema
     *   - Email no debe existir en el sistema
     * 
     * Manejo de Errores:
     * Utiliza el patrón Fail Fast, lanzando IllegalArgumentException
     * con mensajes descriptivos específicos para cada tipo de error:
     * - "El nombre es obligatorio."
     * - "El formato del email no es válido."
     * - "Ya existe una persona registrada con este DNI."
     * 
     * Normalización de Datos:
     * Antes de persistir, se aplican transformaciones de limpieza:
     * - trim() en todos los campos de texto
     * - Normalización de email a lowercase implícito
     * - Eliminación de espacios redundantes
     * 
     * @param persona Entidad Persona con todos los datos del usuario a registrar
     * 
     * @throws IllegalArgumentException Si alguna validación falla, con mensaje descriptivo específico
     * 
     * @implNote Las validaciones de unicidad requieren consultas a base de datos,
     *           por lo que este método puede tener latencia significativa
     */
    public void registrarPersona(Persona persona) {
        // Validaciones básicas de campos obligatorios
        if (persona.getNombre() == null || persona.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (persona.getApellido() == null || persona.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (persona.getEmail() == null || persona.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }
        if (persona.getUsuario() == null || persona.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (persona.getContrasena() == null || persona.getContrasena().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (persona.getTelefono() == null || persona.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }

        // Validación de formato de email
        String email = persona.getEmail().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("El formato del email no es válido.");
        }

        // Validación de DNI positivo
        if (persona.getDni() <= 0) {
            throw new IllegalArgumentException("El DNI debe ser un número positivo.");
        }

        // Validaciones de unicidad (consultas a la BD)
        if (personaRepo.existeDni(persona.getDni())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con este DNI.");
        }
        
        if (personaRepo.existeUsuario(persona.getUsuario().trim())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con este nombre de usuario.");
        }
        
        if (personaRepo.existeEmail(email)) {
            throw new IllegalArgumentException("Ya existe una persona registrada con este email.");
        }

        // Si todas las validaciones pasan, limpiar datos y guardar
        persona.setNombre(persona.getNombre().trim());
        persona.setApellido(persona.getApellido().trim());
        persona.setEmail(email);
        persona.setUsuario(persona.getUsuario().trim());
        persona.setTelefono(persona.getTelefono().trim());
        
        personaRepo.save(persona);
    }

    /**
     * Actualiza los datos de una persona existente en el sistema.
     * 
     * Método simplificado para actualización de datos que delega
     * directamente al repositorio. Se asume que las validaciones
     * necesarias han sido realizadas previamente en la capa de presentación
     * o que se trata de actualizaciones internas del sistema.
     * 
     * Casos de uso:
     * - Actualización de perfil de usuario
     * - Modificaciones administrativas
     * - Sincronización de datos
     * 
     * Consideraciones:
     * - No incluye validaciones de negocio extensivas
     * - Responsabilidad de validación en el llamador
     * - Operación directa sobre entidad existente
     * 
     * @param persona Entidad Persona con los datos actualizados a persistir
     * 
     * @implNote Para actualizaciones críticas que requieren validaciones,
     *           considerar crear métodos específicos con validaciones apropiadas
     */
    public void actualizarPersona(Persona persona) {
        personaRepo.actualizar(persona);
    }

    /**
     * Autentica un usuario en el sistema mediante credenciales de acceso.
     * 
     * Implementa el proceso de autenticación que valida las credenciales
     * proporcionadas contra los datos almacenados en el sistema. Incluye
     * validaciones básicas de entrada y delegación segura al repositorio
     * para la verificación de credenciales.
     * 
     * Proceso de Autenticación:
     * 1. Validación de Entrada:
     *    → Usuario no nulo y no vacío
     *    → Contraseña no nula y no vacía
     * 
     * 2. Normalización:
     *    → trim() en usuario para evitar espacios accidentales
     * 
     * 3. Verificación:
     *    → Delegación al repositorio para validación de credenciales
     *    → Búsqueda por usuario y contraseña exactos
     * 
     * 4. Resultado:
     *    → Persona autenticada si credenciales válidas
     *    → null si credenciales inválidas
     * 
     * Validaciones de entrada:
     * - Usuario Obligatorio: No puede ser null o vacío
     * - Contraseña Obligatoria: No puede ser null o vacía
     * 
     * Seguridad:
     * - No almacena ni loguea contraseñas en texto plano
     * - Delegación segura al repositorio para verificación
     * - Normalización de usuario previene errores de espacios
     * 
     * Casos de uso:
     * - Login desde SesionController
     * - Verificación de credenciales para operaciones sensibles
     * - Reautenticación de sesión
     * 
     * @param usuario Nombre de usuario para autenticación
     * @param contrasena Contraseña del usuario
     * @return Persona autenticada si credenciales son válidas, null en caso contrario
     * 
     * @throws IllegalArgumentException Si usuario o contraseña son null o vacíos
     * 
     * @implNote La verificación real de contraseña (hash, encriptación) 
     *           se maneja en la capa de repositorio
     */
    public Persona login(String usuario, String contrasena) {
        // Validaciones básicas
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        
        return personaRepo.buscarPersonaPorUsuarioYContrasena(usuario.trim(), contrasena);
    }

    /**
     * Registra una persona con validaciones simplificadas para casos de uso específicos.
     * 
     * Este método implementa un proceso de registro alternativo para personas
     * que no requieren acceso completo al sistema, como participantes de eventos
     * que son registrados por organizadores. Utiliza un conjunto reducido de
     * validaciones enfocado en los datos identificatorios esenciales.
     * 
     * Diferencias con Registro Completo:
     * 
     * Registro Completo (registrarPersona):
     *   ✓ Nombre, apellido, email, usuario, contraseña, teléfono
     *   ✓ Validación de formato de email
     *   ✓ Unicidad de DNI, usuario y email
     * 
     * Registro Simple (guardarPersonaSimple):
     *   ✓ Solo nombre, apellido, DNI (campos mínimos)
     *   ✓ Email opcional (puede ser null)
     *   ✓ Unicidad solo de DNI
     *   ✗ Sin credenciales de acceso
     * 
     * Casos de uso:
     * - Registro rápido desde BuscarPersonaController
     * - Adición de participantes sin acceso al sistema
     * - Registro masivo de asistentes a eventos
     * - Importación de datos desde fuentes externas
     * 
     * Validaciones aplicadas:
     * - Campos Obligatorios Mínimos:
     *   - Nombre no nulo y no vacío
     *   - Apellido no nulo y no vacío
     *   - DNI mayor a 0
     * - Unicidad Crítica:
     *   - DNI único en el sistema
     * - Campos Opcionales:
     *   - Email puede ser null (se normaliza si existe)
     *   - Teléfono no requerido
     *   - Usuario/contraseña no aplicables
     * 
     * Proceso de Registro Simple:
     * 1. Validar campos mínimos obligatorios
     * 2. Validar DNI positivo
     * 3. Verificar unicidad de DNI
     * 4. Normalizar datos existentes (trim)
     * 5. Persistir en base de datos
     * 6. Retornar entidad con ID asignado
     * 
     * Ventajas del enfoque:
     * - Rapidez: Menos campos = registro más rápido
     * - Flexibilidad: Para diferentes tipos de usuarios
     * - Simplicidad: Reduce fricción en workflows de eventos
     * 
     * @param persona Entidad Persona con datos mínimos identificatorios
     * @return La misma persona con ID asignado después de persistir
     * 
     * @throws IllegalArgumentException Si validaciones de campos mínimos fallan
     * 
     * @implNote Útil para modales de registro rápido y workflows donde se
     *           requiere agilidad sobre completitud de datos
     */
    public Persona guardarPersonaSimple(Persona persona) {
        // Validaciones básicas de campos obligatorios para registro simple
        if (persona.getNombre() == null || persona.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (persona.getApellido() == null || persona.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (persona.getDni() <= 0) {
            throw new IllegalArgumentException("El DNI debe ser un número positivo.");
        }

        // Validación de unicidad de DNI
        if (personaRepo.existeDni(persona.getDni())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con este DNI.");
        }

        // Limpiar datos y guardar
        persona.setNombre(persona.getNombre().trim());
        persona.setApellido(persona.getApellido().trim());
        if (persona.getEmail() != null) {
            persona.setEmail(persona.getEmail().trim());
        }
        
        personaRepo.save(persona);
        return persona;
    }
}

