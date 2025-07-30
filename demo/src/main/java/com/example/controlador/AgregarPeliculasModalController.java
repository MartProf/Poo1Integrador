package com.example.controlador;

import com.example.modelo.Pelicula;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controlador para el modal de gestión de películas en eventos de Ciclo de Cine.
 * 
 * Este controlador especializado maneja la interfaz para agregar, eliminar y ordenar
 * películas dentro de un evento de tipo Ciclo de Cine. Proporciona una interfaz
 * intuitiva con tabla editable, validaciones de integridad y comunicación bidireccional
 * con el controlador padre para mantener la consistencia de datos.
 * 
 * Funcionalidades Principales:
 * - Gestión de Películas: Agregar/eliminar películas con validaciones
 * - Control de Orden: Secuenciación numérica única para cada película
 * - Validación de Integridad: Campos obligatorios y unicidad de orden
 * - Vista Tabular: Interfaz clara con título y orden de proyección
 * 
 * Patrón de Comunicación:
 * NuevoEventoController (Padre)
 *        ↓ (abre modal)
 * AgregarPeliculasModalController
 *        ↓ (confirma cambios)
 * NuevoEventoController.setPeliculasAgregadas()
 * 
 * Reglas de Negocio Implementadas:
 * - Orden Único: No puede haber dos películas con el mismo orden
 * - Orden Positivo: Los números de orden deben ser mayores a 0
 * - Campos Obligatorios: Título y orden son requeridos
 * - Validación Numérica: El orden debe ser un entero válido
 * 
 * Flujo de Trabajo Típico:
 * 1. Usuario abre "Agregar Películas" desde NuevoEventoController
 * 2. Se cargan películas existentes (si las hay)
 * 3. Usuario agrega/elimina películas según necesidad
 * 4. Validaciones automáticas previenen errores de datos
 * 5. Usuario confirma cambios → actualización en controlador padre
 * 6. Modal se cierra y cambios quedan reflejados en el evento
 * 
 * Integración con el Sistema:
 * Este controlador es específico para eventos de tipo {@link com.example.modelo.CicloDeCine}
 * y se integra estrechamente con:
 * - {@link NuevoEventoController} - Creación/edición de eventos
 * - {@link com.example.modelo.Pelicula} - Entidad de dominio
 * - Archivo FXML: ventanaAgregarPeliculas.fxml
 * 
 * Arquitectura del Modal:
 * Implementa el patrón Modal Editor con las siguientes características:
 * - Estado local temporal hasta confirmación
 * - Validaciones en tiempo real
 * - Cancelación sin efectos secundarios
 * - Confirmación con transferencia de estado al padre
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2024
 * 
 * @see NuevoEventoController
 * @see com.example.modelo.Pelicula
 * @see com.example.modelo.CicloDeCine
 */
public class AgregarPeliculasModalController {

    // ========================================
    // COMPONENTES DE INTERFAZ JAVAFX
    // ========================================
    
    /**
     * Campo de texto para ingresar el título de la película.
     * Acepta cualquier cadena de texto no vacía.
     */
    @FXML private TextField tituloField;
    
    /**
     * Campo de texto para ingresar el orden de proyección de la película.
     * Debe contener un número entero positivo único dentro del ciclo.
     */
    @FXML private TextField ordenField;
    
    /**
     * Tabla que muestra las películas del ciclo con sus títulos y orden.
     * Permite selección para eliminación y visualización del estado actual.
     */
    @FXML private TableView<Pelicula> tablaPeliculas;
    
    /**
     * Columna de la tabla que muestra el título de cada película.
     * Configurada para mostrar el valor de la propiedad 'titulo' de Pelicula.
     */
    @FXML private TableColumn<Pelicula, String> colTitulo;
    
    /**
     * Columna de la tabla que muestra el orden de proyección de cada película.
     * Configurada para mostrar el valor de la propiedad 'orden' de Pelicula.
     */
    @FXML private TableColumn<Pelicula, Integer> colOrden;

    // ========================================
    // ESTADO DEL CONTROLADOR
    // ========================================
    
    /**
     * Lista observable de películas que se está editando en el modal.
     * 
     * Esta lista mantiene el estado temporal de las películas mientras
     * el usuario las edita. Los cambios no se propagan al controlador padre
     * hasta que se confirma la operación con el botón "Aceptar".
     * 
     * Características:
     * - Observable: Cambios se reflejan automáticamente en la tabla
     * - Temporal: Estado local hasta confirmación
     * - Validada: Mantiene integridad de orden único
     */
    private ObservableList<Pelicula> listaPeliculas = FXCollections.observableArrayList();

    /**
     * Referencia al controlador padre que abrió este modal.
     * 
     * Permite comunicación bidireccional para transferir el estado
     * de las películas editadas de vuelta al contexto del evento principal.
     * Se establece mediante el patrón de inyección de dependencias manual.
     * 
     * @see #setControladorPadre(NuevoEventoController)
     * @see #handleAceptar()
     */
    private NuevoEventoController controladorPadre;

    /**
     * Establece la referencia al controlador padre para comunicación bidireccional.
     * 
     * Este método implementa el patrón de inyección de dependencias manual,
     * permitiendo que el modal pueda comunicar los cambios de vuelta al
     * controlador que lo invocó cuando el usuario confirma las modificaciones.
     * 
     * Patrón de Comunicación:
     * // En NuevoEventoController:
     * AgregarPeliculasModalController controller = loader.getController();
     * controller.setControladorPadre(this);  // ← Este método
     * 
     * // Más tarde, en handleAceptar():
     * controladorPadre.setPeliculasAgregadas(listaPeliculas);
     * 
     * @param padre Instancia del controlador que abrió este modal
     * 
     * @implNote Debe llamarse después de cargar el FXML pero antes de mostrar el modal
     */
    public void setControladorPadre(NuevoEventoController padre) {
        this.controladorPadre = padre;
    }

    /**
     * Carga las películas existentes del evento para permitir edición.
     * 
     * Este método inicializa el estado del modal con las películas que
     * ya estaban asociadas al evento, permitiendo al usuario modificar
     * una lista existente en lugar de empezar desde cero.
     * 
     * Casos de Uso:
     * - Evento Nuevo: Se pasa lista vacía o null
     * - Evento Existente: Se cargan películas previamente guardadas
     * - Edición: Usuario puede agregar/eliminar de la lista actual
     * 
     * Proceso de Carga:
     * 1. Validar que hay películas para cargar
     * 2. Limpiar lista actual (por si había datos previos)
     * 3. Agregar todas las películas existentes
     * 4. La tabla se actualiza automáticamente (Observable pattern)
     * 
     * @param peliculasExistentes Lista de películas previamente asociadas al evento,
     *                           puede ser null o vacía para eventos nuevos
     * 
     * @implNote Se debe llamar después de setControladorPadre() pero antes de mostrar el modal
     */
    public void cargarPeliculasExistentes(ObservableList<Pelicula> peliculasExistentes) {
        if (peliculasExistentes != null && !peliculasExistentes.isEmpty()) {
            listaPeliculas.clear();
            listaPeliculas.addAll(peliculasExistentes);
        }
    }

    /**
     * Inicializa el controlador después de que JavaFX haya inyectado todos los componentes FXML.
     * 
     * Este método configura el data binding entre el modelo de datos (Pelicula)
     * y la vista tabular, estableciendo cómo cada propiedad del objeto se mapea
     * a las columnas correspondientes de la tabla.
     * 
     * Configuración de Columnas:
     * - Columna Título: 
     *   - Propiedad: titulo de Pelicula
     *   - Tipo: String via SimpleStringProperty
     *   - Visualización: Texto plano del título
     * - Columna Orden:
     *   - Propiedad: orden de Pelicula
     *   - Tipo: Integer via SimpleIntegerProperty + asObject()
     *   - Visualización: Número entero del orden de proyección
     * 
     * Data Binding Pattern:
     * Pelicula.getTitulo() → SimpleStringProperty → TableColumn texto
     * Pelicula.getOrden()  → SimpleIntegerProperty → TableColumn número
     * 
     * Vinculación de Datos:
     * Establece la lista observable como fuente de datos de la tabla,
     * creando un vínculo automático donde cambios en listaPeliculas se
     * reflejan inmediatamente en la interfaz visual.
     * 
     * @implNote El método asObject() es necesario para convertir primitive int
     *           a Integer wrapper requerido por TableColumn&lt;Pelicula, Integer&gt;
     */
    @FXML
    public void initialize() {
        colTitulo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitulo()));
        colOrden.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getOrden()).asObject());
        tablaPeliculas.setItems(listaPeliculas);
    }

    /**
     * Maneja la adición de una nueva película al ciclo con validaciones completas.
     * 
     * Este método implementa un proceso robusto de validación y creación
     * que asegura la integridad de los datos antes de agregar películas al ciclo.
     * Incluye validaciones de campos, formato numérico y reglas de negocio.
     * 
     * Proceso de Validación:
     * 1. Validación de Campos Obligatorios:
     *    → Título no vacío
     *    → Orden no vacío
     * 
     * 2. Validación de Formato:
     *    → Orden debe ser un número entero válido
     * 
     * 3. Validación de Reglas de Negocio:
     *    → Orden debe ser positivo (> 0)
     *    → Orden debe ser único dentro del ciclo
     * 
     * 4. Creación y Adición:
     *    → Crear nueva instancia de Pelicula
     *    → Agregar a la lista observable
     *    → Limpiar campos para siguiente entrada
     * 
     * Validaciones Implementadas:
     * - Campos Obligatorios: Título y orden requeridos
     * - Formato Numérico: Orden debe ser entero parseable
     * - Rango Válido: Orden debe ser mayor a 0
     * - Unicidad: No puede repetirse el mismo orden
     * 
     * Feedback al Usuario:
     * Cada tipo de error muestra un mensaje específico que ayuda al usuario
     * a comprender y corregir el problema:
     * - "Debe completar ambos campos" → Campos vacíos
     * - "El orden debe ser un número" → Error de formato
     * - "El orden debe ser un número positivo mayor a 0" → Rango inválido
     * - "Ya existe una película con el orden X" → Duplicación
     * 
     * Flujo de Éxito:
     * Cuando todas las validaciones pasan, se crea la película,
     * se agrega a la lista (actualización automática de tabla) y
     * se limpian los campos para facilitar entrada de más películas.
     * 
     * @implNote Utiliza Stream API con anyMatch() para validación eficiente
     *           de unicidad de orden en O(n) tiempo
     */
    @FXML
    private void handleAgregarPelicula() {
        String titulo = tituloField.getText().trim();
        String ordenText = ordenField.getText().trim();

        if (titulo.isEmpty() || ordenText.isEmpty()) {
            mostrarAlerta("Debe completar ambos campos.");
            return;
        }

        int orden;
        try {
            orden = Integer.parseInt(ordenText);
        } catch (NumberFormatException e) {
            mostrarAlerta("El orden debe ser un número.");
            return;
        }

        // Validar que el orden sea positivo
        if (orden <= 0) {
            mostrarAlerta("El orden debe ser un número positivo mayor a 0.");
            return;
        }

        // Validar que no exista ya una película con ese orden
        boolean ordenYaExiste = listaPeliculas.stream()
                .anyMatch(pelicula -> pelicula.getOrden() == orden);
        
        if (ordenYaExiste) {
            mostrarAlerta("Ya existe una película con el orden " + orden + ". Por favor, elija un orden diferente.");
            return;
        }

        Pelicula peli = new Pelicula();
        peli.setTitulo(titulo);
        peli.setOrden(orden);

        listaPeliculas.add(peli);
        tituloField.clear();
        ordenField.clear();
    }

    /**
     * Elimina la película seleccionada de la tabla del ciclo.
     * 
     * Este método proporciona funcionalidad de eliminación segura que
     * requiere selección explícita por parte del usuario. Implementa
     * validación de selección y eliminación inmediata con feedback visual.
     * 
     * Proceso de Eliminación:
     * 1. Obtener película seleccionada del modelo de selección de la tabla
     * 2. Validar que hay una selección activa
     * 3. Si hay selección:
     *    → Eliminar de la lista observable
     *    → Actualización automática de la tabla (Observable pattern)
     * 4. Si no hay selección:
     *    → Mostrar mensaje informativo al usuario
     * 
     * Características de UX:
     * - Eliminación Directa: Sin confirmación adicional (reversible por Cancel)
     * - Feedback Inmediato: Película desaparece de tabla al instante
     * - Validación de Selección: Mensaje claro si no hay selección
     * - Selección Flexible: Usuario puede seleccionar cualquier fila
     * 
     * Consideraciones de Seguridad:
     * La eliminación no requiere confirmación porque:
     * - Es temporal (cambios no se guardan hasta confirmar modal)
     * - Usuario puede cancelar todo el modal sin efectos
     * - La operación es intuitiva y rápida de revertir
     * 
     * @implNote Utiliza el modelo de selección estándar de JavaFX TableView
     *           que maneja automáticamente estados de selección
     */
    @FXML
    private void handleEliminarSeleccionada() {
        Pelicula seleccionada = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            listaPeliculas.remove(seleccionada);
        } else {
            mostrarAlerta("Debe seleccionar una película para eliminar.");
        }
    }

    /**
     * Cancela la operación de edición y cierra el modal sin guardar cambios.
     * 
     * Este método implementa el mecanismo de escape que permite al usuario
     * abandonar la edición sin que los cambios realizados tengan efecto en
     * el evento principal. Proporciona una salida segura sin efectos secundarios.
     * 
     * Comportamiento de Cancelación:
     * - Sin Persistencia: Cambios locales se descartan
     * - Estado Original: El evento mantiene películas previas
     * - Cierre Inmediato: Modal se cierra sin confirmación adicional
     * - Memoria Limpia: Estado temporal del modal se libera
     * 
     * Casos de Uso:
     * - Usuario se arrepiente de cambios realizados
     * - Usuario abrió modal por error
     * - Usuario quiere conservar estado original
     * 
     * @implNote Utiliza navegación por scene graph para obtener Stage y cerrarlo,
     *           patrón estándar en modales JavaFX
     */
    @FXML
    private void handleCancelar() {
        ((Stage) tituloField.getScene().getWindow()).close();
    }

    /**
     * Confirma los cambios realizados y los transfiere al controlador padre.
     * 
     * Este método implementa la confirmación exitosa de la edición,
     * transfiriendo el estado local del modal al contexto del evento principal
     * y cerrando el modal. Es el punto de persistencia de todos los cambios realizados.
     * 
     * Proceso de Confirmación:
     * 1. Crear copia defensiva de la lista de películas editada
     * 2. Transferir copia al controlador padre via setPeliculasAgregadas()
     * 3. Cerrar modal (liberar recursos)
     * 4. Control regresa al NuevoEventoController con datos actualizados
     * 
     * Copia Defensiva:
     * Se crea una nueva ObservableList con los mismos elementos para:
     * - Evitar Referencias Compartidas: Cambios futuros no afectan al padre
     * - Encapsulación: Estado interno del modal queda protegido
     * - Inmutabilidad: Transferencia segura sin efectos laterales
     * 
     * Integración con Padre:
     * El método {@code setPeliculasAgregadas()} del controlador padre
     * recibe la lista y actualiza el estado del evento, reflejando los
     * cambios en la UI principal del formulario de evento.
     * 
     * Flujo Post-Confirmación:
     * Modal se cierra → NuevoEventoController actualiza UI → 
     * Usuario ve cambios reflejados → Puede continuar editando evento
     * 
     * @implNote La copia es necesaria porque ObservableList mantiene listeners
     *           que podrían causar efectos laterales si se comparte la referencia
     */
    @FXML
    private void handleAceptar() {
        // Crear una copia de la lista para enviar al controlador padre
        ObservableList<Pelicula> copiaLista = FXCollections.observableArrayList(listaPeliculas);
        controladorPadre.setPeliculasAgregadas(copiaLista);
        ((Stage) tituloField.getScene().getWindow()).close();
    }

    /**
     * Muestra un diálogo de alerta de advertencia al usuario.
     * 
     * Método utilitario que centraliza la presentación de mensajes de error
     * y advertencia, proporcionando un estilo visual consistente para todas
     * las validaciones y notificaciones del modal.
     * 
     * Características del Diálogo:
     * - Tipo: WARNING (triángulo amarillo con exclamación)
     * - Título: "Atención" (indica tipo de mensaje)
     * - Header: null (sin texto de encabezado redundante)
     * - Modalidad: Bloquea interacción hasta que usuario responde
     * 
     * Casos de Uso en el Controlador:
     * - Campos obligatorios vacíos
     * - Formato numérico inválido en orden
     * - Orden fuera de rango permitido
     * - Orden duplicado en el ciclo
     * - Intento de eliminación sin selección
     * 
     * Estilo Visual:
     * Utiliza AlertType.WARNING que proporciona:
     * - Icono estándar de advertencia del sistema operativo
     * - Colores y tipografía consistentes con el look & feel
     * - Botón "OK" para acknowledgment del usuario
     * 
     * @param mensaje Texto descriptivo del error o advertencia a mostrar al usuario
     * 
     * @implNote showAndWait() bloquea el hilo hasta que el usuario cierra el diálogo,
     *           asegurando que el mensaje sea leído antes de continuar
     */
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
