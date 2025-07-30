package com.example;

/**
 * Clase lanzadora principal para la aplicación municipal de eventos.
 * 
 * Esta clase actúa como punto de entrada alternativo que delega la
 * ejecución a la clase App principal. Proporciona una interfaz limpia
 * y consistente para el inicio de la aplicación, siguiendo el patrón
 * de delegación para mantener la separación de responsabilidades.
 * 
 * Propósito y Arquitectura:
 * - Punto de entrada unificado para diferentes entornos de ejecución
 * - Delegación transparente a la clase App principal
 * - Mantenimiento de compatibilidad con diferentes configuraciones
 * - Facilita la configuración de parámetros de inicio específicos
 * 
 * Casos de Uso:
 * - Ejecución desde IDEs de desarrollo
 * - Lanzamiento desde scripts de deployment
 * - Inicio con configuraciones específicas de entorno
 * - Testing y debugging con parámetros personalizados
 * 
 * Flujo de Ejecución:
 * 1. JVM invoca main() de LanzadorApp
 * 2. Se delegan argumentos a App.main()
 * 3. App inicializa el entorno JavaFX
 * 4. Se carga la aplicación principal
 * 
 * Ventajas del Patrón:
 * - Flexibilidad para futuras extensiones
 * - Punto central para logging de inicio
 * - Posibilidad de preprocessing de argumentos
 * - Manejo centralizado de configuración inicial
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2024
 * 
 * @see App
 */
public class LanzadorApp {
    
    /**
     * Método principal que inicia la aplicación delegando a App.
     * 
     * Este método actúa como proxy transparente hacia la clase App,
     * pasando todos los argumentos de línea de comandos sin modificación.
     * Permite mantener un punto de entrada consistente mientras se
     * preserve la flexibilidad de configuración.
     * 
     * Proceso de Delegación:
     * - Recibe argumentos del sistema operativo
     * - Los transfiere íntegramente a App.main()
     * - App se encarga de la inicialización de JavaFX
     * - Control pasa completamente a la aplicación principal
     * 
     * Argumentos Soportados:
     * Todos los argumentos son transparentemente delegados a App,
     * incluyendo parámetros de configuración, modos de ejecución
     * y opciones específicas de JavaFX si las hubiera.
     * 
     * @param args Argumentos de línea de comandos del sistema operativo,
     *             se pasan sin modificación a la aplicación principal
     * 
     * @implNote La delegación es transparente - no se procesan argumentos localmente
     * @implNote Cualquier excepción se propaga naturalmente hacia el caller
     */
    public static void main(String[] args) {
        App.main(args);
    }
}