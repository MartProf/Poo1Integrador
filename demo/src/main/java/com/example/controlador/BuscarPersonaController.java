package com.example.controlador;

import com.example.modelo.Persona;
import com.example.servicio.PersonaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la interfaz de búsqueda y selección de personas en el sistema municipal de eventos.
 * 
 * Este controlador implementa un modal avanzado de búsqueda que permite a los usuarios encontrar
 * y seleccionar personas registradas en el sistema para asociarlas a eventos como participantes
 * u organizadores. Soporta tanto selección simple como múltiple con filtrado en tiempo real.
 * 
 * Características Principales:
 * - Búsqueda Inteligente: Filtrado en tiempo real por DNI, nombre o apellido
 * - Selección Flexible: Modo simple o múltiple configurable
 * - Registro Integrado: Opción para crear nuevas personas sin salir del modal
 * - UI Responsiva: Lista personalizada con feedback visual y contadores
 * 
 * Patrones de Uso:
 * // Selección simple para organizador de evento
 * BuscarPersonaModalHelper.mostrarModal(false); // → 1 persona
 * 
 * // Selección múltiple para participantes
 * BuscarPersonaModalHelper.mostrarModal(true);  // → N personas
 * 
 * Integración con el Sistema:
 * Este controlador se utiliza desde múltiples puntos del sistema:
 * - NuevoEventoController: Selección de organizadores
 * - EventosDisponiblesController: Registro de participantes
 * - MisEventosController: Gestión de asistentes
 * 
 * Arquitectura UX:
 * Implementa el patrón Modal Search con las siguientes capacidades:
 * - Carga inicial de todas las personas disponibles
 * - Filtrado incremental sin pérdida de contexto
 * - Selección visual con feedback inmediato
 * - Escape hatch para registro de nuevas personas
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2024
 * 
 * @see PersonaService
 * @see BuscarPersonaModalHelper
 * @see RegistroController
 */
public class BuscarPersonaController {

    // ========================================
    // COMPONENTES DE INTERFAZ JAVAFX
    // ========================================
    
    /**
     * Campo de texto para entrada de filtros de búsqueda.
     * Soporta búsqueda por DNI, nombre, apellido o combinaciones.
     */
    @FXML
    private TextField txtFiltro;

    /**
     * Botón para ejecutar búsqueda manual (aunque también funciona en tiempo real).
     */
    @FXML
    private Button btnBuscar;
    
    /**
     * Botón para limpiar filtros y mostrar todas las personas.
     */
    @FXML
    private Button btnLimpiar;

    /**
     * Lista visual de personas que muestra los resultados filtrados.
     * Configurada con celdas personalizadas para mejor UX.
     */
    @FXML
    private ListView<Persona> listResultados;

    /**
     * Botón para confirmar selección y cerrar el modal.
     * Se habilita/deshabilita según el estado de selección.
     */
    @FXML
    private Button btnSeleccionar;

    /**
     * Botón para cancelar la operación y cerrar el modal sin selección.
     */
    @FXML
    private Button btnCancelar;

    /**
     * Botón para abrir el modal de registro de nueva persona.
     * Permite crear personas sin salir del flujo de búsqueda.
     */
    @FXML
    private Button btnRegistrarNueva;
    
    /**
     * Etiqueta con instrucciones dinámicas según el modo de selección.
     */
    @FXML
    private Label lblSubtitulo;
    
    /**
     * Etiqueta con feedback del estado actual de la búsqueda.
     */
    @FXML
    private Label lblResultadosInfo;
    
    /**
     * Etiqueta con contador de resultados encontrados.
     */
    @FXML
    private Label lblContadorResultados;

    // ========================================
    // SERVICIOS Y LÓGICA DE NEGOCIO
    // ========================================
    
    /**
     * Servicio para operaciones CRUD con entidades Persona.
     * Inyectado mediante el patrón Service Locator.
     */
    private final PersonaService personaService;

    // ========================================
    // ESTADO DEL CONTROLADOR
    // ========================================
    
    /**
     * Indica si el modal está configurado para selección múltiple.
     * false = selección simple, true = selección múltiple con Ctrl+Click.
     */
    private boolean multiple = false;

    /**
     * Resultado de selección simple - contiene la persona seleccionada.
     * Solo se usa cuando multiple = false.
     */
    private Persona seleccionada;
    
    /**
     * Resultados de selección múltiple - contiene las personas seleccionadas.
     * Solo se usa cuando multiple = true.
     */
    private List<Persona> seleccionadas = new ArrayList<>();
    
    /**
     * Cache completo de todas las personas en el sistema.
     * Se carga una vez al inicializar y se reutiliza para filtrado local.
     */
    private List<Persona> todasLasPersonas = new ArrayList<>();
    
    /**
     * Lista observable vinculada al ListView para mostrar resultados filtrados.
     * Se actualiza dinámicamente según los filtros aplicados.
     */
    private javafx.collections.ObservableList<Persona> personasFiltradas;

    /**
     * Constructor que inicializa el controlador con las dependencias necesarias.
     * 
     * Utiliza el patrón Service Locator para obtener la instancia del servicio
     * de personas. Este enfoque permite desacoplamiento mientras mantiene simplicidad
     * en la gestión de dependencias.
     * 
     * @implNote El constructor es llamado automáticamente por JavaFX antes de
     *           la inyección de componentes FXML y la llamada a initialize()
     */
    public BuscarPersonaController() {
        this.personaService = new PersonaService();
    }

    /**
     * Inicializa el controlador después de que JavaFX haya inyectado todos los componentes FXML.
     * 
     * Este método configura todo el comportamiento del modal de búsqueda:
     * - Configura el ListView con celdas personalizadas
     * - Inicializa las colecciones observables para data binding
     * - Carga todas las personas disponibles en el sistema
     * - Vincula eventos de botones y listeners de filtrado en tiempo real
     * - Establece el estado inicial de la interfaz
     * 
     * Flujo de Inicialización:
     * 1. Configurar ListView → celdas personalizadas + listeners de selección
     * 2. Inicializar ObservableList → data binding con ListView
     * 3. Cargar datos → todas las personas desde base de datos
     * 4. Vincular eventos → botones + filtrado en tiempo real
     * 5. Estado inicial → botón seleccionar deshabilitado
     * 
     * @implNote Este método es llamado automáticamente por JavaFX después de
     *           cargar el archivo FXML y antes de mostrar la ventana
     */
    public void initialize() {
        // Configurar ListView primero
        configurarListView();
        
        // Inicializar la ObservableList
        personasFiltradas = FXCollections.observableArrayList();
        listResultados.setItems(personasFiltradas);
        
        // Cargar todas las personas
        cargarTodasLasPersonas();
        
        // Configurar eventos de botones
        btnBuscar.setOnAction(e -> aplicarFiltro());
        btnLimpiar.setOnAction(e -> limpiar());
        btnSeleccionar.setOnAction(e -> seleccionar());
        btnCancelar.setOnAction(e -> cerrar());
        btnRegistrarNueva.setOnAction(e -> registrarNuevaPersona());
        
        // Filtrado en tiempo real
        txtFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
            aplicarFiltro();
        });
        
        // Estado inicial
        btnSeleccionar.setDisable(true);
    }

    /**
     * Configura la apariencia y comportamiento del ListView de resultados.
     * 
     * Este método implementa una fábrica de celdas personalizada que transforma
     * cada objeto Persona en una representación visual rica con iconos, formato
     * estructurado y estilos CSS. También configura los listeners necesarios
     * para el manejo de selección.
     * 
     * Características de las Celdas Personalizadas:
     * - Formato Visual: "👤 Nombre Apellido (DNI: 12345678)"
     * - Protección de Datos: Maneja valores null con fallbacks
     * - Estilo Consistente: Padding, fuentes y bordes uniformes
     * - Estados Visuales: Diferentes estilos para celdas vacías/llenas
     * 
     * Listeners Configurados:
     * - Selección: Habilita/deshabilita botón según selección
     * - Items: Debug listener para cambios en la colección
     * 
     * @implNote Utiliza el patrón Factory Method para crear celdas personalizadas
     *           y el patrón Observer para reaccionar a cambios de selección
     */
    private void configurarListView() {
        // Configurar cómo se muestra cada persona en la lista
        listResultados.setCellFactory(param -> new ListCell<Persona>() {
            @Override
            protected void updateItem(Persona persona, boolean empty) {
                super.updateItem(persona, empty);
                if (empty || persona == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Verificar que los datos existen
                    String nombre = persona.getNombre() != null ? persona.getNombre() : "SIN_NOMBRE";
                    String apellido = persona.getApellido() != null ? persona.getApellido() : "SIN_APELLIDO";
                    int dni = persona.getDni();
                    
                    String texto = String.format("👤 %s %s (DNI: %s)", nombre, apellido, dni);
                    setText(texto);
                    setStyle("-fx-padding: 10; -fx-font-size: 14px; -fx-background-color: #f9f9f9; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
                }
            }
        });
        
        // Listener para selección (habilitar/deshabilitar botón)
        listResultados.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                btnSeleccionar.setDisable(newValue == null);
            }
        );
        
        // Debug: Listener para cambios en los items
        listResultados.itemsProperty().addListener((observable, oldValue, newValue) -> {});
    }

    /**
     * Carga todas las personas disponibles en el sistema y las muestra en la interfaz.
     * 
     * Este método implementa una estrategia de carga eager donde todas las personas
     * se obtienen de una vez y se almacenan en cache local para permitir filtrado
     * instantáneo sin consultas adicionales a la base de datos.
     * 
     * Flujo de Carga:
     * 1. Mostrar indicador de carga → "Cargando personas..."
     * 2. Obtener datos → PersonaService.obtenerTodas()
     * 3. Actualizar cache → todasLasPersonas = resultado
     * 4. Actualizar UI → personasFiltradas.setAll() + refresh()
     * 5. Feedback → contador + mensaje de estado
     * 
     * Ventajas del Enfoque Eager:
     * - Filtrado Instantáneo: Sin latencia en búsquedas
     * - Trabajo Offline: Datos disponibles sin conexión continua
     * - UX Fluida: Respuesta inmediata a interacciones del usuario
     * 
     * Manejo de Errores:
     * Captura excepciones durante la carga y muestra mensajes de error apropiados
     * en la interfaz, permitiendo que el usuario comprenda el problema sin crashes.
     * 
     * @implNote Utiliza setAll() en lugar de clear() + addAll() para minimizar
     *           eventos de cambio y mejorar performance de la UI
     */
    private void cargarTodasLasPersonas() {
        try {
            lblResultadosInfo.setText("Cargando personas...");
            
            // Cargar todas las personas del sistema
            todasLasPersonas = personaService.obtenerTodas();
            
            // Actualizar la ObservableList existente
            personasFiltradas.setAll(todasLasPersonas);
            
            // Forzar refresh del ListView
            listResultados.refresh();
            
            actualizarContadorResultados(todasLasPersonas.size());
            
            lblResultadosInfo.setText("✅ Todas las personas cargadas - Puede buscar escribiendo en el campo");
            
        } catch (Exception e) {
            lblResultadosInfo.setText("❌ Error cargando personas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Aplica filtros de búsqueda en tiempo real sobre el cache de personas.
     * 
     * Este método implementa un algoritmo de búsqueda flexible que permite
     * encontrar personas utilizando múltiples criterios de forma simultánea.
     * El filtrado se realiza completamente en memoria para máxima responsividad.
     * 
     * Criterios de Búsqueda Soportados:
     * - DNI: Búsqueda exacta por número de documento
     * - Nombre: Búsqueda case-insensitive por nombre
     * - Apellido: Búsqueda case-insensitive por apellido
     * - Nombre Completo: Búsqueda en "Nombre Apellido"
     * 
     * Lógica de Filtrado:
     * Si filtro está vacío:
     *   → Mostrar todas las personas (vista completa)
     * Si filtro tiene contenido:
     *   → Aplicar OR lógico entre todos los criterios
     *   → DNI contains filtro OR nombre contains filtro OR apellido contains filtro OR nombreCompleto contains filtro
     * 
     * Características UX:
     * - Feedback Inmediato: Actualización visual instantánea
     * - Tolerancia a Espacios: trim() automático de entrada
     * - Case Insensitive: Búsqueda independiente de mayúsculas
     * - Contadores Dinámicos: Número de resultados en tiempo real
     * 
     * @implNote Utiliza Stream API con predicados compuestos para filtrado eficiente
     *           y toList() para compatibilidad con Java 17+
     */
    private void aplicarFiltro() {
        String filtro = txtFiltro.getText();
        
        if (filtro == null || filtro.trim().isEmpty()) {
            // Mostrar todas las personas
            personasFiltradas.setAll(todasLasPersonas);
            lblResultadosInfo.setText("✅ Mostrando todas las personas");
        } else {
            // Filtrar por DNI o nombre
            String filtroLower = filtro.trim().toLowerCase();
            List<Persona> resultadosFiltrados = todasLasPersonas.stream()
                .filter(persona -> 
                    String.valueOf(persona.getDni()).contains(filtro) ||
                    persona.getNombre().toLowerCase().contains(filtroLower) ||
                    persona.getApellido().toLowerCase().contains(filtroLower) ||
                    (persona.getNombre() + " " + persona.getApellido()).toLowerCase().contains(filtroLower)
                )
                .toList();
            
            personasFiltradas.setAll(resultadosFiltrados);
            
            if (resultadosFiltrados.isEmpty()) {
                lblResultadosInfo.setText("❌ No se encontraron personas que coincidan con '" + filtro + "'");
            } else {
                lblResultadosInfo.setText("✅ Filtrado por: '" + filtro + "'");
            }
        }
        
        actualizarContadorResultados(personasFiltradas.size());
        
        }

    /**
     * Limpia todos los filtros aplicados y restaura la vista completa de personas.
     * 
     * Este método proporciona un mecanismo de "reset" que permite al usuario
     * volver rápidamente al estado inicial sin perder el contexto de la búsqueda.
     * Es especialmente útil cuando los filtros han reducido demasiado los resultados.
     * 
     * Acciones Realizadas:
     * - Limpiar Campo: txtFiltro.clear() → campo vacío
     * - Restaurar Vista: personasFiltradas = todasLasPersonas
     * - Actualizar Contador: Mostrar total de personas
     * - Feedback Visual: Mensaje confirmando la limpieza
     * 
     * Protección contra Errores:
     * Incluye validaciones de null para evitar NullPointerException en casos
     * donde la inicialización no haya completado correctamente.
     * 
     * @implNote Se ejecuta tanto por acción directa del botón como por efectos
     *           secundarios de otros métodos que requieren reset de estado
     */
    private void limpiar() {
        txtFiltro.clear();
        // Al limpiar, mostrar todas las personas nuevamente
        if (personasFiltradas != null && todasLasPersonas != null) {
            personasFiltradas.setAll(todasLasPersonas);
            actualizarContadorResultados(todasLasPersonas.size());
            lblResultadosInfo.setText("✅ Filtro limpiado - Mostrando todas las personas");
        }
    }

    /**
     * Actualiza los elementos de feedback visual con el número actual de resultados.
     * 
     * Este método centraliza la lógica de actualización de contadores y estado
     * de botones, asegurando consistencia en toda la interfaz cuando cambia
     * el número de resultados visibles.
     * 
     * Elementos Actualizados:
     * - Contador Visual: "X persona(s) encontrada(s)"
     * - Estado de Botón: Deshabilitar si no hay resultados
     * - Pluralización: Gramática correcta según cantidad
     * 
     * Lógica de Estado:
     * Si cantidad == 0:
     *   → Deshabilitar botón seleccionar (no hay qué seleccionar)
     * Si cantidad > 0:
     *   → Habilitar botón seleccionar (hay opciones disponibles)
     * 
     * @param cantidad Número actual de personas en los resultados filtrados
     * 
     * @implNote Utiliza operador ternario para pluralización gramaticalmente correcta
     */
    private void actualizarContadorResultados(int cantidad) {
        String texto = cantidad + (cantidad == 1 ? " persona encontrada" : " personas encontradas");
        lblContadorResultados.setText(texto);
        btnSeleccionar.setDisable(cantidad == 0);
        
    }

    /**
     * Configura el modo de selección del modal (simple vs múltiple).
     * 
     * Este método permite que el mismo controlador y vista FXML sean reutilizados
     * para diferentes casos de uso en el sistema, adaptando dinámicamente el
     * comportamiento según las necesidades del contexto que lo invoca.
     * 
     * Modos de Selección:
     * - Simple (false): Selección de una sola persona
     *   - Usado para: Seleccionar organizador de evento
     *   - UI: Click normal para seleccionar
     *   - Resultado: Una sola persona en getSeleccionada()
     * - Múltiple (true): Selección de múltiples personas
     *   - Usado para: Seleccionar participantes de evento
     *   - UI: Ctrl+Click para selección múltiple
     *   - Resultado: Lista de personas en getSeleccionadas()
     * 
     * Adaptaciones de UI:
     * Modo Simple:
     *   - Subtítulo: "Buscar por DNI, nombre o apellido"
     *   - Botón: "✅ Seleccionar Persona"
     *   - Comportamiento: Un solo click selecciona
     * 
     * Modo Múltiple:
     *   - Subtítulo: "Seleccionar múltiples personas (Ctrl+Click...)"
     *   - Botón: "✅ Seleccionar Personas"
     *   - Comportamiento: Ctrl+Click para múltiples
     * 
     * @param multiple true para selección múltiple, false para selección simple
     * 
     * @implNote Este método debe llamarse ANTES de mostrar el modal para
     *           asegurar que la UI refleje correctamente el modo seleccionado
     */
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
        listResultados.getSelectionModel().setSelectionMode(
                multiple ? SelectionMode.MULTIPLE : SelectionMode.SINGLE
        );
        
        // Actualizar textos según el modo
        if (multiple) {
            lblSubtitulo.setText("Seleccionar múltiples personas (Ctrl+Click para selección múltiple)");
            btnSeleccionar.setText("✅ Seleccionar Personas");
        } else {
            lblSubtitulo.setText("Buscar por DNI, nombre o apellido");
            btnSeleccionar.setText("✅ Seleccionar Persona");
        }
    }

    /**
     * Procesa la selección de personas y cierra el modal con los resultados.
     * 
     * Este método implementa la lógica diferenciada para manejar tanto
     * selección simple como múltiple, validando que se haya realizado
     * una selección válida antes de confirmar la operación.
     * 
     * Flujo de Selección:
     * Si modo múltiple:
     *   1. Obtener selección múltiple del ListView
     *   2. Validar que hay al menos una persona seleccionada
     *   3. Copiar selección a la lista de resultados
     *   4. Cerrar modal
     * 
     * Si modo simple:
     *   1. Obtener persona seleccionada del ListView
     *   2. Validar que hay una persona seleccionada
     *   3. Asignar a variable de resultado
     *   4. Cerrar modal
     * 
     * Validaciones Implementadas:
     * - Selección Vacía: Muestra alerta si no hay selección
     * - Protección de Estado: Diferentes validaciones por modo
     * - Feedback de Error: Mensajes específicos por situación
     * 
     * Gestión de Resultados:
     * Los resultados quedan disponibles mediante los métodos getter después
     * del cierre del modal, permitiendo al código invocador recuperar la selección.
     * 
     * @implNote Utiliza new ArrayList<>(seleccion) para crear una copia defensiva
     *           y evitar problemas de referencia compartida
     */
    private void seleccionar() {
        if (multiple) {
            List<Persona> seleccion = listResultados.getSelectionModel().getSelectedItems();
            if (seleccion.isEmpty()) {
                mostrarAlerta("Debe seleccionar al menos una persona.");
                return;
            }
            seleccionadas = new ArrayList<>(seleccion);
        } else {
            Persona p = listResultados.getSelectionModel().getSelectedItem();
            if (p == null) {
                mostrarAlerta("Debe seleccionar una persona.");
                return;
            }
            seleccionada = p;
        }
        cerrar();
    }

    /**
     * Cierra el modal de búsqueda liberando los recursos de la ventana.
     * 
     * Este método implementa el patrón de cierre estándar para modales JavaFX,
     * obteniendo la referencia al Stage desde el scene graph y cerrándolo
     * apropiadamente para liberar recursos y notificar al código invocador.
     * 
     * Proceso de Cierre:
     * - Obtener Stage: Navegación por scene graph hasta la ventana
     * - Cerrar Ventana: stage.close() para cierre limpio
     * - Liberar Recursos: Permitir garbage collection
     * 
     * Integración con Modal Pattern:
     * Después del cierre, el control regresa al código que invocó
     * showAndWait(), permitiendo procesar los resultados de la selección.
     * 
     * @implNote Utiliza el botón cancelar como punto de referencia para
     *           obtener el Stage, técnica estándar en JavaFX
     */
    private void cerrar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    /**
     * Obtiene la persona seleccionada en modo de selección simple.
     * 
     * Este método debe usarse únicamente cuando el modal fue configurado
     * en modo simple (setMultiple(false)). Proporciona acceso al resultado
     * de la selección después de que el modal ha sido cerrado.
     * 
     * Contrato de Uso:
     * // Configurar y mostrar modal
     * controller.setMultiple(false);
     * stage.showAndWait();
     * 
     * // Obtener resultado
     * Persona seleccionada = controller.getSeleccionada();
     * if (seleccionada != null) {
     *     // Procesar persona seleccionada
     * }
     * 
     * @return La persona seleccionada, o null si se canceló o no se seleccionó nada
     * 
     * @implNote Solo válido después de cerrar el modal en modo simple
     */
    public Persona getSeleccionada() {
        return seleccionada;
    }

    /**
     * Obtiene la lista de personas seleccionadas en modo de selección múltiple.
     * 
     * Este método debe usarse únicamente cuando el modal fue configurado
     * en modo múltiple (setMultiple(true)). Devuelve una lista con todas
     * las personas que fueron seleccionadas con Ctrl+Click.
     * 
     * Contrato de Uso:
     * // Configurar y mostrar modal
     * controller.setMultiple(true);
     * stage.showAndWait();
     * 
     * // Obtener resultados
     * List<Persona> seleccionadas = controller.getSeleccionadas();
     * if (!seleccionadas.isEmpty()) {
     *     // Procesar personas seleccionadas
     * }
     * 
     * @return Lista de personas seleccionadas, vacía si se canceló o no se seleccionó nada
     * 
     * @implNote Solo válido después de cerrar el modal en modo múltiple
     */
    public List<Persona> getSeleccionadas() {
        return seleccionadas;
    }

    /**
     * Muestra un diálogo de alerta informativa al usuario.
     * 
     * Método utilitario para mostrar mensajes de error o información
     * de manera consistente a través de toda la interfaz del modal.
     * Utiliza el estilo visual estándar de JavaFX para alertas.
     * 
     * Características del Diálogo:
     * - Tipo: INFORMATION (icono i azul)
     * - Header: null (sin texto de encabezado)
     * - Modalidad: Bloquea interacción hasta cerrar
     * - Comportamiento: showAndWait() espera respuesta del usuario
     * 
     * @param mensaje Texto a mostrar en el cuerpo del diálogo de alerta
     * 
     * @implNote Utilizado principalmente para validaciones de selección
     */
    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Abre el modal de registro para crear una nueva persona sin salir del flujo de búsqueda.
     * 
     * Este método implementa un "escape hatch" que permite a los usuarios
     * crear nuevas personas cuando no encuentran a quien buscan, manteniendo
     * la continuidad del flujo de trabajo sin obligar a salir y reiniciar
     * el proceso de selección.
     * 
     * Flujo de Registro Integrado:
     * 1. Cargar FXML del formulario de registro existente
     * 2. Configurar RegistroController en modo modal (sin usuario/contraseña)
     * 3. Mostrar modal de registro con showAndWait()
     * 4. Si se registró exitosamente:
     *    a. Obtener la persona recién creada
     *    b. Seleccionarla automáticamente según el modo actual
     *    c. Cerrar el modal de búsqueda con la selección
     * 5. Si se canceló: Continuar en el modal de búsqueda
     * 
     * Ventajas de la Integración:
     * - Flujo Continuo: Sin interrupciones en la experiencia del usuario
     * - Selección Automática: La persona creada queda preseleccionada
     * - Modo Adaptativo: Respeta el modo simple/múltiple del contexto
     * - Reutilización: Aprovecha el formulario de registro existente
     * 
     * Configuración Modal:
     * Utiliza setModoModal(true) para omitir campos de usuario/contraseña
     * que no son relevantes en el contexto de registro rápido de participantes.
     * 
     * Manejo de Errores:
     * Captura y maneja errores de carga del FXML, mostrando mensajes
     * informativos sin crashes que degraden la experiencia del usuario.
     * 
     * @implNote Utiliza APPLICATION_MODAL para bloquear interacción con el
     *           modal de búsqueda mientras se registra la nueva persona
     */
    private void registrarNuevaPersona() {
        try {
            // Cargar el formulario de registro existente
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/example/registro.fxml")
            );
            javafx.scene.Parent root = loader.load();

            RegistroController registroController = loader.getController();
            
            // Configurar el controller en modo modal (sin usuario/contraseña)
            registroController.setModoModal(true);

            // Crear y mostrar el modal
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Registrar Nueva Persona");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            // Obtener la persona registrada
            Persona nuevaPersona = registroController.getPersonaRegistrada();
            if (nuevaPersona != null) {
                // Seleccionar automáticamente la persona recién creada
                if (multiple) {
                    seleccionadas.clear();
                    seleccionadas.add(nuevaPersona);
                } else {
                    seleccionada = nuevaPersona;
                }
                // Cerrar el modal principal
                cerrar();
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al abrir el formulario de registro: " + e.getMessage());
        }
    }
}
