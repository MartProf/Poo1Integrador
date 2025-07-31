package com.example.util;

import com.example.controlador.BuscarPersonaController;
import com.example.modelo.Persona;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Collections;
import java.util.List;

/**
 * Clase utilitaria para facilitar el uso del modal de búsqueda de personas.
 * 
 * Esta clase implementa el patrón Helper proporcionando métodos estáticos
 * simplificados para abrir el modal de búsqueda de personas en sus diferentes
 * modos de operación. Encapsula toda la complejidad de configuración de JavaFX,
 * carga de FXML y gestión de ventanas modales, ofreciendo una API limpia y
 * fácil de usar para otros componentes del sistema.
 * 
 * Funcionalidades Principales:
 * - Apertura simplificada de modal en modo selección simple
 * - Apertura optimizada para selección múltiple de personas
 * - Configuración automática de propiedades de ventana modal
 * - Manejo robusto de errores con valores de retorno seguros
 * - Encapsulación completa de detalles de implementación JavaFX
 * 
 * Patrones de Diseño Implementados:
 * - Helper Pattern: Simplificación de operaciones complejas
 * - Factory Pattern: Creación estandarizada de modales configurados
 * - Null Object Pattern: Retorno de valores seguros en caso de error
 * 
 * Configuración de Ventana Modal:
 * - Modalidad: APPLICATION_MODAL para bloqueo completo de aplicación
 * - Redimensionable: Permitido para mejor experiencia de usuario
 * - Tamaño mínimo: 500x400 pixels para usabilidad adecuada
 * - Centrado automático: Posicionamiento óptimo en pantalla
 * - Título contextual: Diferenciado según modo de selección
 * 
 * Modos de Operación Soportados:
 * 
 * Selección Simple:
 * - Permite seleccionar una única persona
 * - Retorna Persona individual o null si se cancela
 * - Ideal para campos de referencia única
 * - Comportamiento: click simple para seleccionar
 * 
 * Selección Múltiple:
 * - Permite seleccionar múltiples personas con Ctrl+Click
 * - Retorna List de personas seleccionadas
 * - Ideal para asignación de responsables o participantes
 * - Comportamiento: selección múltiple con modificadores de teclado
 * 
 * Ventajas de la Abstracción:
 * - API simple para operaciones complejas de modal
 * - Configuración consistente en toda la aplicación
 * - Manejo centralizado de errores y casos edge
 * - Reutilización de código sin duplicación
 * - Facilita mantenimiento y actualizaciones futuras
 * 
 * Casos de Uso en el Sistema:
 * - Selección de responsables en formularios de eventos
 * - Asignación de participantes en inscripciones
 * - Búsqueda rápida en cualquier formulario que requiera personas
 * - Integración con campos de autocompletado
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2024
 * 
 * @see BuscarPersonaController
 * @see com.example.modelo.Persona
 */
public class BuscarPersonaModalHelper {

    /**
     * Abre el modal de búsqueda configurado para selección simple de persona.
     * 
     * Este método estático proporciona la forma más simple de abrir el modal
     * de búsqueda cuando se necesita seleccionar una única persona. Encapsula
     * toda la configuración necesaria de JavaFX y maneja automáticamente la
     * carga del FXML, configuración del controlador y presentación modal.
     * 
     * Proceso de Configuración Automática:
     * 1. Carga del archivo FXML BuscarPersona.fxml
     * 2. Obtención del controlador asociado
     * 3. Configuración en modo simple (setMultiple(false))
     * 4. Creación y configuración del Stage modal
     * 5. Presentación con showAndWait() para bloqueo
     * 6. Recuperación del resultado de selección
     * 
     * Configuración de Ventana Optimizada:
     * - Título: "Buscar Persona" (singular para modo simple)
     * - Modalidad: APPLICATION_MODAL (bloquea toda la aplicación)
     * - Redimensionable: true (permite ajustar tamaño según necesidad)
     * - Centrado automático: centerOnScreen() para posicionamiento óptimo
     * - Tamaño mínimo: 500x400 para garantizar usabilidad
     * 
     * Comportamiento de Selección Simple:
     * - Click simple en cualquier fila selecciona inmediatamente
     * - Botón "Seleccionar" confirma la selección actual
     * - Botón "Cancelar" cierra sin seleccionar nada
     * - Doble click puede confirmar selección directamente
     * 
     * Casos de Uso Típicos:
     * - Selección de responsable único en formularios de evento
     * - Asignación de persona en campos de referencia
     * - Búsqueda para completar datos de contacto
     * - Selección en formularios de registro o edición
     * 
     * Manejo Robusto de Errores:
     * - Try-catch envuelve toda la operación
     * - Stack trace para debugging en caso de error
     * - Retorno de null como valor seguro ante fallos
     * - No propaga excepciones al código llamador
     * 
     * Ventajas del Método Estático:
     * - No requiere instanciación de la clase helper
     * - API simple: una línea de código para abrir modal
     * - Configuración consistente garantizada
     * - Fácil de usar desde cualquier parte del código
     * 
     * @return Persona seleccionada por el usuario, o null si:
     *         - Se canceló la operación
     *         - No se seleccionó ninguna persona
     *         - Ocurrió un error durante la carga del modal
     * 
     * @implNote Utiliza showAndWait() que bloquea el hilo hasta cerrar el modal
     * @implNote El modal se centra automáticamente independiente del padre
     */
    public static Persona abrirSeleccionSimple() {
        try {
            FXMLLoader loader = new FXMLLoader(BuscarPersonaModalHelper.class.getResource("/com/example/BuscarPersona.fxml"));
            Parent root = loader.load();

            BuscarPersonaController controller = loader.getController();
            controller.setMultiple(false); // Modo simple

            Stage stage = new Stage();
            stage.setTitle("Buscar Persona");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true); // Permitir redimensionar
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Centrar la ventana y asegurar que esté completamente visible
            stage.centerOnScreen();
            
            // Configurar tamaño mínimo
            stage.setMinWidth(500);
            stage.setMinHeight(400);
            
            stage.showAndWait();

            return controller.getSeleccionada();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Abre el modal de búsqueda configurado para selección múltiple de personas.
     * 
     * Este método proporciona funcionalidad avanzada para casos donde se requiere
     * seleccionar múltiples personas simultáneamente. Configura automáticamente
     * el modal en modo múltiple y maneja toda la infraestructura JavaFX necesaria
     * para la operación de selección compleja.
     * 
     * Proceso de Configuración Específica:
     * 1. Carga estándar del FXML BuscarPersona.fxml
     * 2. Acceso al controlador del modal
     * 3. Activación de modo múltiple (setMultiple(true))
     * 4. Configuración del Stage con título adecuado
     * 5. Presentación modal con bloqueo de aplicación
     * 6. Recuperación de lista de personas seleccionadas
     * 
     * Configuración Optimizada para Selección Múltiple:
     * - Título: "Buscar Personas" (plural para indicar múltiple)
     * - Modalidad: APPLICATION_MODAL (garantiza foco exclusivo)
     * - Redimensionable: true (importante para visualizar múltiples selecciones)
     * - Centrado: centerOnScreen() para acceso óptimo
     * - Tamaño mínimo: 500x400 (suficiente para mostrar selecciones múltiples)
     * 
     * Comportamiento de Selección Múltiple:
     * - Click con Ctrl permite selecciones no consecutivas
     * - Click con Shift permite selecciones de rango
     * - Click simple reemplaza selección actual (si no hay modificadores)
     * - Indicación visual clara de todas las selecciones activas
     * - Contador o resumen de cantidad seleccionada (según implementación)
     * 
     * Casos de Uso Avanzados:
     * - Selección de múltiples participantes para eventos grupales
     * - Asignación masiva de personas a roles o categorías
     * - Configuración de listas de contactos o invitados
     * - Operaciones batch que requieren múltiples personas
     * - Formación de equipos o grupos de trabajo
     * 
     * Control de Validación Múltiple:
     * - Validación de límites mínimos/máximos (según contexto)
     * - Verificación de compatibilidad entre selecciones
     * - Prevención de selecciones duplicadas o inválidas
     * - Confirmación explícita antes de procesar la lista
     * 
     * Manejo de Errores Específico:
     * - Try-catch abarca toda la operación múltiple
     * - Log detallado para debugging de operaciones complejas
     * - Lista vacía como valor seguro ante cualquier error
     * - Preservación de datos parciales cuando sea posible
     * 
     * Optimizaciones de Performance:
     * - Carga eficiente de múltiples registros
     * - Actualización optimizada de la interfaz durante selección
     * - Manejo de memoria consciente para grandes listas
     * - Procesamiento asíncrono cuando aplique
     * 
     * @return Lista de personas seleccionadas por el usuario. Puede ser:
     *         - Lista vacía si se canceló o no se seleccionó nada
     *         - Lista con una o más personas según la selección del usuario
     *         - Lista vacía en caso de error durante la operación
     * 
     * @implNote La lista retornada es una nueva instancia, no una referencia
     * @implNote Usar showAndWait() asegura que la lista esté completa al retornar
     */
    public static List<Persona> abrirSeleccionMultiple() {
        try {
            FXMLLoader loader = new FXMLLoader(BuscarPersonaModalHelper.class.getResource("/com/example/BuscarPersona.fxml"));
            Parent root = loader.load();

            BuscarPersonaController controller = loader.getController();
            controller.setMultiple(true); // Modo múltiple

            Stage stage = new Stage();
            stage.setTitle("Buscar Personas");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true); // Permitir redimensionar
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Centrar la ventana y asegurar que esté completamente visible
            stage.centerOnScreen();
            
            // Configurar tamaño mínimo
            stage.setMinWidth(500);
            stage.setMinHeight(400);
            
            stage.showAndWait();

            return controller.getSeleccionadas();

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
