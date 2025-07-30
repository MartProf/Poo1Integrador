package com.example.controlador;

import com.example.modelo.*;
import com.example.servicio.EventoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión municipal de eventos del sistema.
 * 
 * Este controlador implementa una interfaz completa de administración que permite
 * a los funcionarios municipales visualizar, filtrar, buscar, editar y eliminar
 * todos los eventos del sistema, proporcionando una vista panorámica de la
 * gestión cultural municipal.
 * 
 * Funcionalidades principales:
 * - Visualización tabular completa de todos los eventos del sistema
 * - Sistema de búsqueda en tiempo real por nombre de evento
 * - Filtrado avanzado por tipo, estado y responsable
 * - Estadísticas dinámicas actualizadas según filtros aplicados
 * - Operaciones CRUD: edición y eliminación con confirmación
 * - Información detallada específica por tipo de evento
 * - Gestión de responsables con vista multi-persona
 * 
 * Características técnicas:
 * - Tabla dinámica con columnas configurables
 * - Filtros combinados que se aplican automáticamente
 * - Estadísticas calculadas en tiempo real
 * - Integración con modal de edición de eventos
 * - Sistema de confirmación para operaciones destructivas
 * 
 * El controlador opera sobre TODOS los eventos del sistema (no solo los
 * del usuario logueado), proporcionando una perspectiva administrativa
 * completa para la gestión municipal.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 * @see EventoService
 * @see NuevoEventoController
 */
public class MisEventosController {

    // =============== COMPONENTES DE TABLA PRINCIPAL ===============
    
    /**
     * Tabla principal que muestra todos los eventos del sistema.
     * 
     * Configurada con múltiples columnas que incluyen información
     * básica y específica de cada tipo de evento, además de acciones
     * de gestión (editar/eliminar).
     */
    @FXML
    private TableView<Evento> tablaMisEventos;

    /**
     * Columna que muestra el nombre del evento.
     * 
     * Campo principal de identificación, utilizado también
     * como criterio principal de búsqueda textual.
     */
    @FXML
    private TableColumn<Evento, String> colNombre;
    
    /**
     * Columna que muestra la fecha de inicio del evento.
     * 
     * Formato estándar ISO (YYYY-MM-DD) para consistencia
     * en ordenamiento y visualización.
     */
    @FXML
    private TableColumn<Evento, String> colFecha;
    
    /**
     * Columna que muestra la duración en días del evento.
     * 
     * Valor numérico que indica cuántos días consecutivos
     * durará el evento desde la fecha de inicio.
     */
    @FXML
    private TableColumn<Evento, String> colDuracion;
    
    /**
     * Columna que muestra el tipo específico de evento.
     * 
     * Identifica la clase concreta (Feria, Concierto, Exposición,
     * Taller, Ciclo de Cine) para diferenciación visual.
     */
    @FXML
    private TableColumn<Evento, String> colTipo;
    
    /**
     * Columna que muestra información adicional específica por tipo.
     * 
     * Contiene datos relevantes según el tipo de evento:
     * - Feria: cantidad de stands y ubicación
     * - Concierto: política de entrada y cantidad de artistas
     * - Exposición: tipo de arte y curador
     * - Taller: cupo, modalidad e instructor
     * - Ciclo de Cine: charlas y cantidad de películas
     */
    @FXML
    private TableColumn<Evento, String> colInfoAdicional;
    
    /**
     * Columna que indica si el evento requiere inscripción.
     * 
     * Muestra "Sí" para eventos con gestión de participantes
     * (especialmente Talleres) o "No" para eventos abiertos.
     */
    @FXML
    private TableColumn<Evento, String> colTieneInscripcion;
    
    /**
     * Columna que muestra la cantidad actual de inscriptos.
     * 
     * Número de participantes registrados en el evento,
     * importante para eventos con cupo limitado.
     */
    @FXML
    private TableColumn<Evento, String> colCantidadInscriptos;
    
    /**
     * Columna que muestra el estado actual del evento.
     * 
     * Estados posibles: Planificado, Confirmado, En Ejecución, Finalizado.
     * Formateado en español para mejor comprensión del usuario.
     */
    @FXML
    private TableColumn<Evento, String> colEstado;
    
    /**
     * Columna de acciones con botones de editar y eliminar.
     * 
     * Implementa CellFactory personalizado que genera botones
     * dinámicos para cada fila con operaciones específicas.
     */
    @FXML
    private TableColumn<Evento, Void> colAcciones;
    
    /**
     * Columna que muestra los responsables del evento.
     * 
     * Muestra el nombre completo si hay un solo responsable,
     * o indica la cantidad si hay múltiples responsables.
     */
    @FXML
    private TableColumn<Evento, String> colResponsables;

    // =============== PANEL DE ESTADÍSTICAS DINÁMICAS ===============
    
    /**
     * Etiqueta que muestra el total de eventos en el sistema.
     * 
     * Se actualiza dinámicamente según los filtros aplicados:
     * - Sin filtros: total de eventos del sistema
     * - Con filtros: total de eventos que cumplen criterios
     */
    @FXML
    private Label lblTotalEventos;
    
    /**
     * Etiqueta que muestra la cantidad de eventos activos.
     * 
     * Considera como activos los eventos en estado CONFIRMADO
     * y EN_EJECUCION, excluyendo planificados y finalizados.
     */
    @FXML
    private Label lblEventosActivos;
    
    /**
     * Etiqueta que muestra el total de participantes inscriptos.
     * 
     * Suma todos los participantes de todos los eventos visibles,
     * proporcionando una métrica de engagement ciudadano.
     */
    @FXML
    private Label lblTotalParticipantes;
    
    /**
     * Etiqueta que muestra eventos próximos (próximos 7 días).
     * 
     * Cuenta eventos cuya fecha de inicio esté entre hoy
     * y los próximos 7 días, útil para planificación inmediata.
     */
    @FXML
    private Label lblEventosProximos;

    // =============== CONTROLES DE BÚSQUEDA ===============
    
    /**
     * Campo de texto para búsqueda en tiempo real por nombre.
     * 
     * Implementa filtrado automático mientras el usuario tipea,
     * realizando búsqueda case-insensitive en nombres de eventos.
     */
    @FXML
    private TextField txtBusqueda;
    
    /**
     * Botón para ejecutar búsqueda manualmente.
     * 
     * Complementa la búsqueda automática, permite re-ejecutar
     * la búsqueda o aplicar filtros combinados.
     */
    @FXML
    private Button btnBuscar;
    
    /**
     * Botón para limpiar únicamente el campo de búsqueda.
     * 
     * Mantiene los filtros activos pero elimina el criterio
     * de búsqueda textual, mostrando todos los eventos filtrados.
     */
    @FXML
    private Button btnLimpiarBusqueda;

    // =============== CONTROLES DE FILTRADO AVANZADO ===============
    
    /**
     * ComboBox para filtrar eventos por tipo.
     * 
     * Opciones: Todos, Feria, Concierto, Exposición, Taller, Ciclo de Cine.
     * Permite visualizar solo eventos de un tipo específico.
     */
    @FXML
    private ComboBox<String> cmbTipo;
    
    /**
     * ComboBox para filtrar eventos por estado.
     * 
     * Opciones: Todos, Planificado, Confirmado, En Ejecución, Finalizado.
     * Útil para gestionar eventos según su etapa en el ciclo de vida.
     */
    @FXML
    private ComboBox<String> cmbEstado;
    
    /**
     * ComboBox para filtrar eventos por responsable.
     * 
     * Se llena dinámicamente con todos los responsables únicos
     * del sistema, permitiendo ver eventos de personas específicas.
     */
    @FXML
    private ComboBox<String> cmbResponsable;
    
    /**
     * Botón para aplicar manualmente todos los filtros.
     * 
     * Aunque los filtros se aplican automáticamente al cambiar
     * las selecciones, este botón permite re-aplicar si es necesario.
     */
    @FXML
    private Button btnAplicarFiltros;
    
    /**
     * Botón para limpiar todos los filtros y búsqueda.
     * 
     * Restaura la vista completa del sistema eliminando todos
     * los criterios de filtrado y búsqueda aplicados.
     */
    @FXML
    private Button btnLimpiarFiltros;

    // =============== INFORMACIÓN Y ACCIONES GENERALES ===============
    
    /**
     * Etiqueta informativa sobre los resultados mostrados.
     * 
     * Indica cuántos eventos se están mostrando actualmente,
     * proporcionando contexto sobre el filtrado aplicado.
     */
    @FXML
    private Label lblResultados;
    
    /**
     * Botón para actualizar manualmente los datos desde la base.
     * 
     * Recarga todos los eventos del sistema, útil cuando se
     * realizan cambios desde otras partes de la aplicación.
     */
    @FXML
    private Button btnActualizar;

    // =============== ESTADO INTERNO Y SERVICIOS ===============
    
    /**
     * Persona autenticada actualmente en el sistema.
     * 
     * Aunque este controlador muestra todos los eventos (no solo
     * del usuario), se mantiene referencia para futuras funcionalidades
     * como permisos específicos o auditoría de operaciones.
     */
    private Persona personaLogueada;
    
    /**
     * Servicio para operaciones de persistencia de eventos.
     * 
     * Utilizado para cargar todos los eventos, actualizar y eliminar.
     * Inicializado en el constructor para disponibilidad inmediata.
     */
    private EventoService eventoService;
    
    // =============== LISTAS DE DATOS PARA FILTRADO ===============
    
    /**
     * Lista maestra con todos los eventos del sistema.
     * 
     * Contiene la referencia completa sin filtros, utilizada como
     * base para aplicar criterios de búsqueda y filtrado.
     */
    private List<Evento> todosLosEventos;
    
    /**
     * Lista observable filtrada mostrada en la tabla.
     * 
     * Subconjunto de todosLosEventos que cumple con los criterios
     * de búsqueda y filtrado actuales, vinculada a la tabla JavaFX.
     */
    private ObservableList<Evento> eventosFiltrados;

    /**
     * Constructor del controlador de gestión de eventos.
     * 
     * Inicializa el servicio de eventos para las operaciones de persistencia.
     * La configuración específica de la interfaz se realiza posteriormente
     * en el método initialize() y setPersonaLogueada().
     */
    public MisEventosController() {
        this.eventoService = new EventoService();
    }

    /**
     * Determina si un evento requiere proceso de inscripción.
     * 
     * Lógica de inscripción:
     * - Talleres: SIEMPRE requieren inscripción (por gestión de cupo)
     * - Otros tipos: Requieren si tienen participantes registrados
     * 
     * Esta información es crucial para la gestión municipal ya que
     * indica qué eventos necesitan seguimiento de participantes.
     * 
     * @param evento El evento a evaluar
     * @return "Sí" si requiere inscripción, "No" en caso contrario
     */
    private String determinarSiRequiereInscripcion(Evento evento) {
        // Los talleres siempre requieren inscripción (por el cupo)
        if (evento instanceof Taller) {
            return "Sí";
        }
        
        // Los otros tipos pueden o no requerir inscripción según la lógica de negocio
        // Por ahora, asumo que todos pueden tener participantes opcionales
        return evento.getParticipantes() != null && !evento.getParticipantes().isEmpty() ? "Sí" : "No";
    }

    /**
     * Formatea los estados de evento para visualización en español.
     * 
     * Convierte los valores del enum EstadoEvento a texto amigable
     * para mejor comprensión del usuario final y consistencia
     * en la interfaz de gestión municipal.
     * 
     * @param estado El estado del evento a formatear
     * @return Texto formateado en español del estado
     */
    private String formatearEstado(EstadoEvento estado) {
        switch (estado) {
            case PLANIFICADO:
                return "Planificado";
            case CONFIRMADO:
                return "Confirmado";
            case EN_EJECUCION:
                return "En Ejecución";
            case FINALIZADO:
                return "Finalizado";
            default:
                return estado.toString();
        }
    }

    /**
     * Configura la persona autenticada y carga datos iniciales.
     * 
     * Aunque este controlador gestiona TODOS los eventos del sistema
     * (no solo los del usuario), se mantiene la referencia para
     * futuras funcionalidades como auditoría y permisos específicos.
     * 
     * Dispara automáticamente la carga completa de eventos del sistema.
     * 
     * @param persona La persona autenticada en el sistema
     * @see #cargarTodosLosEventos()
     */
    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        // FASE 2: Cambiar a cargar todos los eventos para gestión municipal
        cargarTodosLosEventos();
    }

    /**
     * Inicializa todos los componentes de la interfaz después de cargar el FXML.
     * 
     * Proceso de inicialización en tres fases:
     * 1. Configuración de columnas de tabla con factories específicas
     * 2. Inicialización de ComboBoxes con valores por defecto
     * 3. Configuración de manejadores de eventos para interactividad
     * 4. Establecimiento de estadísticas iniciales en cero
     * 
     * Se ejecuta automáticamente por JavaFX antes de mostrar la ventana.
     * 
     * @see #initColumns()
     * @see #initComboBoxes()
     * @see #initEventHandlers()
     * @see #actualizarEstadisticas()
     */
    @FXML
    public void initialize() {
        initColumns();
        initComboBoxes();
        initEventHandlers();
        // Inicializar estadísticas en 0 hasta que se carguen los datos
        actualizarEstadisticas();
    }

    /**
     * Configura todas las columnas de la tabla principal.
     * 
     * Configuración específica por columna:
     * - Columnas básicas: Utilizan PropertyValueFactory simple
     * - Columnas calculadas: Implementan lógica específica de formateo
     * - Columna de información adicional: Aplica lógica específica por tipo
     * - Columna de acciones: Implementa CellFactory con botones dinámicos
     * 
     * La configuración incluye manejo de excepciones LazyInitializationException
     * para relaciones JPA que podrían no estar cargadas.
     * 
     * @see #obtenerTipoEvento(Evento)
     * @see #obtenerInfoAdicional(Evento)
     * @see #getBotonAccionesCellFactory()
     */
    private void initColumns() {
        // Columna Nombre
        colNombre.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        
        // Columna Fecha
        colFecha.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFechaInicio().toString()));
        
        // Columna Duración
        colDuracion.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getDuraciónDias())));
        
        // Columna Tipo de Evento
        colTipo.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                obtenerTipoEvento(data.getValue())));
        
        // Columna Información Adicional
        colInfoAdicional.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                obtenerInfoAdicional(data.getValue())));
        
        // Columna ¿Tiene inscripción?
        colTieneInscripcion.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                determinarSiRequiereInscripcion(data.getValue())));
        
        // Columna Cantidad de Inscriptos
        colCantidadInscriptos.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                String.valueOf(obtenerCantidadInscriptos(data.getValue()))));
        
        // Columna Estado
        colEstado.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                formatearEstado(data.getValue().getEstado())));

        // Columna Responsables (nueva)
        colResponsables.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                obtenerResponsables(data.getValue())));

        // Columna Acciones (botones)
        colAcciones.setCellFactory(getBotonAccionesCellFactory());
    }

    /**
     * Determina el tipo específico de evento usando instanceof.
     * 
     * Identifica la clase concreta del evento para mostrar en la
     * columna de tipo, proporcionando diferenciación visual clara
     * entre los diferentes tipos de eventos del sistema.
     * 
     * @param evento El evento cuyo tipo se desea determinar
     * @return Nombre del tipo de evento en español
     */
    private String obtenerTipoEvento(Evento evento) {
        // Usando instanceof para identificar el tipo específico
        if (evento instanceof Feria) {
            return "Feria";
        } else if (evento instanceof Concierto) {
            return "Concierto";
        } else if (evento instanceof Exposicion) {
            return "Exposición";
        } else if (evento instanceof Taller) {
            return "Taller";
        } else if (evento instanceof CicloDeCine) {
            return "Ciclo de Cine";
        } else {
            return "Evento";
        }
    }

    /**
     * Genera información adicional específica según el tipo de evento.
     * 
     * Proporciona datos relevantes y específicos para cada tipo:
     * 
     * FERIA:
     * - Cantidad de stands disponibles
     * - Ubicación (al aire libre vs techada)
     * 
     * CONCIERTO:
     * - Política de entrada (gratuita vs paga)
     * - Cantidad de artistas participantes
     * 
     * EXPOSICIÓN:
     * - Tipo de arte exhibido
     * - Curador responsable asignado
     * 
     * TALLER:
     * - Cupo máximo de participantes
     * - Modalidad de dictado (presencial/virtual/híbrida)
     * - Instructor responsable
     * 
     * CICLO DE CINE:
     * - Disponibilidad de charlas posteriores
     * - Cantidad de películas programadas
     * 
     * Implementa manejo defensivo para LazyInitializationException
     * que puede ocurrir con relaciones JPA no cargadas.
     * 
     * @param evento El evento cuya información adicional se desea obtener
     * @return Cadena formateada con información específica del tipo
     */
    private String obtenerInfoAdicional(Evento evento) {
        if (evento instanceof Feria) {
            Feria feria = (Feria) evento;
            return String.format("Stands: %d, %s", 
                feria.getCantidadDeStand(),
                feria.isAlAirelibre() ? "Al aire libre" : "Techada");
                
        } else if (evento instanceof Concierto) {
            Concierto concierto = (Concierto) evento;
            int cantidadArtistas = 0;
            try {
                cantidadArtistas = concierto.getArtistas() != null ? concierto.getArtistas().size() : 0;
            } catch (Exception e) {
                // LazyInitializationException resuelto en EventoRepository.findAllWithRelations()
                cantidadArtistas = 0;
            }
            return String.format("Entrada: %s, Artistas: %d", 
                concierto.isEntradaGratuita() ? "Gratuita" : "Paga",
                cantidadArtistas);
                
        } else if (evento instanceof Exposicion) {
            Exposicion exposicion = (Exposicion) evento;
            return String.format("Arte: %s, Curador: %s", 
                exposicion.getTipoArte(),
                exposicion.getCurador() != null ? exposicion.getCurador().toString() : "Sin asignar");
                
        } else if (evento instanceof Taller) {
            Taller taller = (Taller) evento;
            return String.format("Cupo: %d, %s, Instructor: %s", 
                taller.getCupoMaximo(),
                taller.getModalidad() != null ? taller.getModalidad().toString() : "No definida",
                taller.getInstructor() != null ? taller.getInstructor().toString() : "Sin asignar");
                
        } else if (evento instanceof CicloDeCine) {
            CicloDeCine ciclo = (CicloDeCine) evento;
            int cantidadPeliculas = 0;
            try {
                cantidadPeliculas = ciclo.getPeliculas() != null ? ciclo.getPeliculas().size() : 0;
            } catch (Exception e) {
                // LazyInitializationException resuelto en EventoRepository.findAllWithRelations()
                cantidadPeliculas = 0;
            }
            return String.format("%s, Películas: %d", 
                ciclo.isHayCharlas() ? "Con charlas" : "Sin charlas",
                cantidadPeliculas);
                
        } else {
            return "Ver detalles";
        }
    }

    /**
     * Obtiene la cantidad de participantes inscriptos en el evento.
     * 
     * Cuenta el número de participantes registrados, información
     * crucial para eventos con cupo limitado (especialmente Talleres)
     * y para estadísticas de participación ciudadana.
     * 
     * @param evento El evento cuya cantidad de inscriptos se desea obtener
     * @return Número de participantes inscriptos (0 si no hay participantes)
     */
    private int obtenerCantidadInscriptos(Evento evento) {
        // Verificar si el evento tiene participantes
        if (evento.getParticipantes() != null) {
            return evento.getParticipantes().size();
        }
        return 0;
    }

    /**
     * Genera información sobre los responsables del evento.
     * 
     * Formato de visualización:
     * - Un responsable: Muestra nombre completo (nombre + apellido)
     * - Múltiples responsables: Muestra cantidad ("X responsables")
     * - Sin responsables: Muestra "Sin asignar"
     * 
     * Esta información es fundamental para la gestión municipal
     * ya que permite identificar rápidamente quién está a cargo
     * de cada evento.
     * 
     * @param evento El evento cuyos responsables se desean mostrar
     * @return Cadena formateada con información de responsables
     */
    private String obtenerResponsables(Evento evento) {
        // Obtener lista de responsables del evento
        if (evento.getResponsables() != null && !evento.getResponsables().isEmpty()) {
            if (evento.getResponsables().size() == 1) {
                Persona responsable = evento.getResponsables().get(0);
                return responsable.getNombre() + " " + responsable.getApellido();
            } else {
                // Si hay múltiples responsables, mostrar cantidad
                return evento.getResponsables().size() + " responsables";
            }
        }
        return "Sin asignar";
    }

    /**
     * Inicializa los ComboBoxes de filtrado con valores predeterminados.
     * 
     * Configuración específica:
     * - ComboBox de tipos: Incluye "Todos" más cada tipo específico de evento
     * - ComboBox de estados: Incluye "Todos" más estados formateados en español
     * - ComboBox de responsables: Se llena dinámicamente después de cargar datos
     * 
     * Todos los ComboBoxes inician con "Todos" seleccionado para mostrar
     * la vista completa del sistema por defecto.
     */
    private void initComboBoxes() {
        // Inicializar ComboBox de tipos
        cmbTipo.setItems(FXCollections.observableArrayList(
            "Todos", "Feria", "Concierto", "Exposición", "Taller", "Ciclo de Cine"
        ));
        cmbTipo.getSelectionModel().select("Todos");

        // Inicializar ComboBox de estados
        cmbEstado.setItems(FXCollections.observableArrayList(
            "Todos", "Planificado", "Confirmado", "En Ejecución", "Finalizado"
        ));
        cmbEstado.getSelectionModel().select("Todos");

        // ComboBox de responsables se llenará dinámicamente
        cmbResponsable.setItems(FXCollections.observableArrayList("Todos"));
        cmbResponsable.getSelectionModel().select("Todos");
    }

    /**
     * Configura todos los manejadores de eventos para interactividad.
     * 
     * Configuración de eventos:
     * 
     * BÚSQUEDA:
     * - Botón buscar: Aplica filtros manualmente
     * - Botón limpiar búsqueda: Limpia solo texto de búsqueda
     * - Campo de texto: Listener para búsqueda en tiempo real
     * 
     * FILTROS:
     * - Botón aplicar filtros: Re-aplica todos los filtros
     * - Botón limpiar filtros: Restaura vista completa
     * - ComboBoxes: Listeners para filtrado automático al cambiar selección
     * 
     * ACCIONES:
     * - Botón actualizar: Recarga datos desde la base de datos
     * 
     * Los filtros automáticos proporcionan experiencia fluida donde
     * los cambios se aplican inmediatamente sin necesidad de botones.
     */
    private void initEventHandlers() {
        // Eventos de búsqueda
        if (btnBuscar != null) {
            btnBuscar.setOnAction(e -> aplicarBusqueda());
        }
        if (btnLimpiarBusqueda != null) {
            btnLimpiarBusqueda.setOnAction(e -> limpiarBusqueda());
        }
        
        // Búsqueda en tiempo real
        if (txtBusqueda != null) {
            txtBusqueda.textProperty().addListener((observable, oldValue, newValue) -> {
                aplicarBusqueda();
            });
        }

        // Eventos de filtros
        if (btnAplicarFiltros != null) {
            btnAplicarFiltros.setOnAction(e -> aplicarFiltros());
        }
        if (btnLimpiarFiltros != null) {
            btnLimpiarFiltros.setOnAction(e -> limpiarFiltros());
        }
        
        // FASE 4: Filtros automáticos al cambiar ComboBox
        if (cmbTipo != null) {
            cmbTipo.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> aplicarFiltros());
        }
        if (cmbEstado != null) {
            cmbEstado.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> aplicarFiltros());
        }
        if (cmbResponsable != null) {
            cmbResponsable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> aplicarFiltros());
        }

        // Evento de actualizar
        if (btnActualizar != null) {
            btnActualizar.setOnAction(e -> cargarTodosLosEventos());
        }
    }

    /**
     * Inicializa las estadísticas con valores cero.
     * 
     * Método sobrecargado para la inicialización inicial antes de
     * cargar datos. Evita mostrar valores incorrectos mientras
     * se cargan los eventos desde la base de datos.
     */
    private void actualizarEstadisticas() {
        // Método sobrecargado para inicialización
        actualizarEstadisticas(FXCollections.emptyObservableList());
    }

    /**
     * Calcula y actualiza todas las estadísticas del panel.
     * 
     * Métricas calculadas:
     * 
     * TOTAL DE EVENTOS:
     * - Cuenta todos los eventos en la lista proporcionada
     * - Refleja filtros aplicados si los hay
     * 
     * EVENTOS ACTIVOS:
     * - Estados CONFIRMADO y EN_EJECUCION únicamente
     * - Excluye eventos planificados y finalizados
     * 
     * TOTAL DE PARTICIPANTES:
     * - Suma participantes de todos los eventos visibles
     * - Métrica de engagement ciudadano
     * 
     * EVENTOS PRÓXIMOS:
     * - Eventos con fecha de inicio en los próximos 7 días
     * - Útil para planificación operativa inmediata
     * 
     * Las estadísticas son dinámicas y se actualizan según los
     * filtros aplicados, proporcionando contexto específico.
     * 
     * @param eventos Lista de eventos para calcular estadísticas
     */
    private void actualizarEstadisticas(List<Evento> eventos) {
        if (eventos == null) eventos = FXCollections.emptyObservableList();
        
        // Calcular estadísticas reales
        int totalEventos = eventos.size();
        
        // Contar eventos activos (Confirmado y En Ejecución)
        int eventosActivos = (int) eventos.stream()
            .filter(e -> e.getEstado() == EstadoEvento.CONFIRMADO || 
                        e.getEstado() == EstadoEvento.EN_EJECUCION)
            .count();
        
        // Contar total de participantes
        int totalParticipantes = eventos.stream()
            .mapToInt(e -> e.getParticipantes() != null ? e.getParticipantes().size() : 0)
            .sum();
        
        // Contar eventos próximos (próximos 7 días)
        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalDate proximaSemana = hoy.plusDays(7);
        int eventosProximos = (int) eventos.stream()
            .filter(e -> e.getFechaInicio() != null && 
                        !e.getFechaInicio().isBefore(hoy) && 
                        !e.getFechaInicio().isAfter(proximaSemana))
            .count();
        
        // Actualizar labels
        if (lblTotalEventos != null) lblTotalEventos.setText(String.valueOf(totalEventos));
        if (lblEventosActivos != null) lblEventosActivos.setText(String.valueOf(eventosActivos));
        if (lblTotalParticipantes != null) lblTotalParticipantes.setText(String.valueOf(totalParticipantes));
        if (lblEventosProximos != null) lblEventosProximos.setText(String.valueOf(eventosProximos));
    }

    /**
     * Actualiza la información sobre resultados mostrados.
     * 
     * Proporciona feedback al usuario sobre cuántos eventos
     * están siendo visualizados actualmente, ayudando a entender
     * el impacto de los filtros aplicados.
     * 
     * @param eventos Lista actual de eventos mostrados
     */
    private void actualizarInformacionResultados(List<Evento> eventos) {
        if (eventos == null) eventos = FXCollections.emptyObservableList();
        String mensaje = String.format("Mostrando %d evento%s", 
                                      eventos.size(), 
                                      eventos.size() == 1 ? "" : "s");
        if (lblResultados != null) {
            lblResultados.setText(mensaje);
        }
    }

    // =============== SISTEMA DE BÚSQUEDA Y FILTRADO ===============
    
    /**
     * Aplica búsqueda textual integrada con el sistema de filtros.
     * 
     * La búsqueda forma parte del sistema de filtrado integral,
     * por lo que delega la funcionalidad al método aplicarFiltros()
     * que maneja todos los criterios de manera combinada.
     * 
     * @see #aplicarFiltros()
     */
    private void aplicarBusqueda() {
        // FASE 4: Integrar búsqueda con filtros
        aplicarFiltros(); // Ahora la búsqueda es parte del sistema de filtros integral
    }

    /**
     * Limpia únicamente el campo de búsqueda textual.
     * 
     * Mantiene todos los filtros activos (tipo, estado, responsable)
     * pero elimina el criterio de búsqueda por nombre, permitiendo
     * ver todos los eventos que cumplen con los filtros restantes.
     */
    private void limpiarBusqueda() {
        // FASE 4: Limpiar solo búsqueda, mantener filtros
        if (txtBusqueda != null) {
            txtBusqueda.clear();
        }
        
        // Reaplicar filtros sin búsqueda
        aplicarFiltros();
    }

    /**
     * Método central del sistema de filtrado combinado.
     * 
     * Este método implementa un sistema de filtrado integral que combina
     * múltiples criterios de manera secuencial y eficiente:
     * 
     * FASE 1 - RECOLECCIÓN DE CRITERIOS:
     * - Obtiene selecciones actuales de todos los ComboBoxes
     * - Captura texto de búsqueda del campo correspondiente
     * - Inicializa con todos los eventos disponibles
     * 
     * FASE 2 - APLICACIÓN SECUENCIAL DE FILTROS:
     * - Filtro de búsqueda textual: case-insensitive en nombres
     * - Filtro por tipo: usando obtenerTipoEvento() para comparación
     * - Filtro por estado: usando formatearEstado() para comparación
     * - Filtro por responsable: búsqueda en lista de responsables
     * 
     * FASE 3 - ACTUALIZACIÓN DE INTERFAZ:
     * - Actualiza tabla con eventos filtrados
     * - Refresca información de resultados
     * - Determina si usar estadísticas globales o filtradas
     * 
     * LÓGICA DE ESTADÍSTICAS:
     * - Sin filtros aplicados: Muestra estadísticas globales del sistema
     * - Con filtros aplicados: Muestra estadísticas de eventos filtrados
     * 
     * El filtrado es no destructivo, siempre parte de la lista maestra
     * y genera una nueva lista filtrada sin modificar los datos originales.
     * 
     * @see #obtenerTipoEvento(Evento)
     * @see #formatearEstado(EstadoEvento)
     * @see #actualizarEstadisticas(List)
     */
    private void aplicarFiltros() {
        // FASE 4: Implementar filtros combinados
        if (todosLosEventos == null || eventosFiltrados == null) return;
        
        String tipoSeleccionado = cmbTipo.getSelectionModel().getSelectedItem();
        String estadoSeleccionado = cmbEstado.getSelectionModel().getSelectedItem();
        String responsableSeleccionado = cmbResponsable.getSelectionModel().getSelectedItem();
        String textoBusqueda = txtBusqueda.getText();
        
        // Empezar con todos los eventos
        List<Evento> eventosFiltradosTemp = new java.util.ArrayList<>(todosLosEventos);
        
        // Aplicar filtro de búsqueda por nombre
        if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
            String busquedaLower = textoBusqueda.trim().toLowerCase();
            eventosFiltradosTemp = eventosFiltradosTemp.stream()
                .filter(evento -> evento.getNombre().toLowerCase().contains(busquedaLower))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Aplicar filtro por tipo
        if (tipoSeleccionado != null && !tipoSeleccionado.equals("Todos")) {
            eventosFiltradosTemp = eventosFiltradosTemp.stream()
                .filter(evento -> obtenerTipoEvento(evento).equals(tipoSeleccionado))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Aplicar filtro por estado
        if (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos")) {
            eventosFiltradosTemp = eventosFiltradosTemp.stream()
                .filter(evento -> formatearEstado(evento.getEstado()).equals(estadoSeleccionado))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Aplicar filtro por responsable
        if (responsableSeleccionado != null && !responsableSeleccionado.equals("Todos")) {
            eventosFiltradosTemp = eventosFiltradosTemp.stream()
                .filter(evento -> {
                    if (evento.getResponsables() == null) return false;
                    return evento.getResponsables().stream()
                        .anyMatch(persona -> 
                            (persona.getNombre() + " " + persona.getApellido()).equals(responsableSeleccionado));
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Actualizar tabla y estadísticas
        eventosFiltrados.setAll(eventosFiltradosTemp);
        actualizarInformacionResultados(eventosFiltrados);
        
        // FASE 4: Actualizar estadísticas según filtros aplicados
        // Si no hay filtros aplicados, usar estadísticas globales, sino usar filtradas
        boolean hayFiltrosAplicados = 
            (tipoSeleccionado != null && !tipoSeleccionado.equals("Todos")) ||
            (estadoSeleccionado != null && !estadoSeleccionado.equals("Todos")) ||
            (responsableSeleccionado != null && !responsableSeleccionado.equals("Todos")) ||
            (textoBusqueda != null && !textoBusqueda.trim().isEmpty());
            
        if (hayFiltrosAplicados) {
            // Mostrar estadísticas de los eventos filtrados
            actualizarEstadisticas(eventosFiltradosTemp);
        } else {
            // Mostrar estadísticas globales
            actualizarEstadisticas(todosLosEventos);
        }
    }

    /**
     * Limpia todos los filtros y restaura la vista completa del sistema.
     * 
     * Operaciones de limpieza:
     * 1. Restaura todos los ComboBoxes a "Todos"
     * 2. Limpia el campo de búsqueda textual
     * 3. Restablece la lista filtrada con todos los eventos
     * 4. Actualiza información de resultados y estadísticas globales
     * 
     * Proporciona un botón de "reset" completo para volver rápidamente
     * a la vista panorámica sin filtros aplicados.
     */
    private void limpiarFiltros() {
        // FASE 4: Limpiar todos los filtros y búsqueda
        if (cmbTipo != null) cmbTipo.getSelectionModel().select("Todos");
        if (cmbEstado != null) cmbEstado.getSelectionModel().select("Todos");
        if (cmbResponsable != null) cmbResponsable.getSelectionModel().select("Todos");
        if (txtBusqueda != null) txtBusqueda.clear();
        
        // Restablecer todos los eventos
        if (eventosFiltrados != null && todosLosEventos != null) {
            eventosFiltrados.setAll(todosLosEventos);
            actualizarInformacionResultados(eventosFiltrados);
            // Restablecer estadísticas globales
            actualizarEstadisticas(todosLosEventos);
        }
    }

    /**
     * Carga todos los eventos del sistema desde la base de datos.
     * 
     * Proceso de carga completo:
     * 1. Obtiene TODOS los eventos del sistema (no filtrados por usuario)
     * 2. Inicializa la lista filtrada con todos los eventos
     * 3. Vincula la lista a la tabla para visualización
     * 4. Actualiza ComboBox de responsables con datos dinámicos
     * 5. Recalcula estadísticas globales y información de resultados
     * 
     * Este método implementa la perspectiva municipal completa,
     * mostrando todos los eventos independientemente del usuario logueado.
     * 
     * @see EventoService#getTodosLosEventos()
     * @see #actualizarComboBoxResponsables()
     */
    private void cargarTodosLosEventos() {
        // FASE 2: Cargar TODOS los eventos del sistema
        todosLosEventos = eventoService.getTodosLosEventos();
        
        // Inicializar la lista filtrada con todos los eventos
        eventosFiltrados = FXCollections.observableArrayList(todosLosEventos);
        tablaMisEventos.setItems(eventosFiltrados);
        
        // FASE 4: Llenar ComboBox de responsables dinámicamente
        actualizarComboBoxResponsables();
        
        // Actualizar estadísticas y resultados
        actualizarEstadisticas(todosLosEventos);
        actualizarInformacionResultados(eventosFiltrados);
    }

    /**
     * Actualiza dinámicamente el ComboBox de responsables.
     * 
     * Proceso de actualización:
     * 1. Extrae todos los responsables únicos de todos los eventos
     * 2. Genera lista de nombres completos sin duplicados
     * 3. Ordena alfabéticamente para mejor usabilidad
     * 4. Agrega "Todos" al inicio de la lista
     * 5. Actualiza el ComboBox y selecciona "Todos" por defecto
     * 
     * Esta funcionalidad permite filtrar eventos por responsable específico,
     * fundamental para la gestión municipal donde se necesita ver qué
     * eventos está manejando cada funcionario.
     */
    private void actualizarComboBoxResponsables() {
        // FASE 4: Llenar ComboBox con responsables únicos
        if (cmbResponsable != null && todosLosEventos != null) {
            // Obtener todos los responsables únicos
            List<String> responsablesUnicos = todosLosEventos.stream()
                .flatMap(evento -> evento.getResponsables() != null ? 
                    evento.getResponsables().stream() : java.util.stream.Stream.empty())
                .distinct()
                .map(persona -> persona.getNombre() + " " + persona.getApellido())
                .sorted()
                .collect(java.util.stream.Collectors.toList());
            
            // Agregar "Todos" al inicio
            responsablesUnicos.add(0, "Todos");
            
            // Actualizar ComboBox
            cmbResponsable.setItems(FXCollections.observableArrayList(responsablesUnicos));
            cmbResponsable.getSelectionModel().select("Todos");
        }
    }

    /**
     * Genera CellFactory para la columna de acciones con botones dinámicos.
     * 
     * Implementa TableCell personalizado que:
     * 1. Crea botones de "Editar" y "Eliminar" para cada fila
     * 2. Aplica estilos diferenciados (verde para editar, rojo para eliminar)
     * 3. Configura manejadores de eventos específicos por acción
     * 4. Organiza botones horizontalmente con espaciado apropiado
     * 5. Maneja correctamente filas vacías sin mostrar botones
     * 
     * Los botones son dinámicos y se generan para cada fila de la tabla,
     * proporcionando acceso directo a operaciones CRUD sin menús contextuales.
     * 
     * @return CellFactory configurado para generar botones de acción
     * @see #editarEvento(Evento)
     * @see #eliminarEvento(Evento)
     */
    private Callback<TableColumn<Evento, Void>, TableCell<Evento, Void>> getBotonAccionesCellFactory() {
        return param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");

            {
                // Estilos para los botones
                btnEditar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnEliminar.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                btnEditar.setOnAction(event -> {
                    Evento evento = getTableView().getItems().get(getIndex());
                    editarEvento(evento);
                });

                btnEliminar.setOnAction(event -> {
                    Evento evento = getTableView().getItems().get(getIndex());
                    eliminarEvento(evento);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5, btnEditar, btnEliminar);
                    setGraphic(hbox);
                }
            }
        };
    }

    /**
     * Abre el modal de edición para un evento específico.
     * 
     * Proceso de edición:
     * 1. Carga el FXML del controlador de nuevo evento
     * 2. Obtiene referencia al controlador y configura modo edición
     * 3. Pre-carga todos los datos del evento en el formulario
     * 4. Configura ventana modal con dimensiones apropiadas
     * 5. Espera a que se complete la edición antes de continuar
     * 6. Recarga automáticamente la tabla para reflejar cambios
     * 
     * La integración con NuevoEventoController permite reutilizar
     * toda la lógica de validación y persistencia existente,
     * manteniendo consistencia en la experiencia de usuario.
     * 
     * @param evento El evento a editar
     * @see NuevoEventoController#setModoEdicion(boolean)
     * @see NuevoEventoController#cargarEventoParaEditar(Evento)
     */
    private void editarEvento(Evento evento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/nuevoEvento.fxml"));
            Parent root = loader.load();

            NuevoEventoController controller = loader.getController();
            controller.setModoEdicion(true);
            controller.cargarEventoParaEditar(evento);

            Stage stage = new Stage();
            stage.setTitle("Editar Evento");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true); //Permitir redimensionar
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Configurar tamaño optimal para evitar problemas con barra de tareas
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setMinWidth(700);
            stage.setMinHeight(500);
            
            // Centrar la ventana automáticamente
            stage.centerOnScreen();
            
            stage.showAndWait();

            cargarTodosLosEventos(); // Recargar tabla después de editar

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Maneja la eliminación de eventos con confirmación del usuario.
     * 
     * Proceso de eliminación seguro:
     * 1. Muestra dialog de confirmación con mensaje claro
     * 2. Utiliza botones Sí/No para decisión explícita del usuario
     * 3. Solo procede con eliminación si el usuario confirma
     * 4. Invoca servicio de eliminación para persistencia
     * 5. Recarga automáticamente la tabla para reflejar cambios
     * 6. Cancela operación si el usuario selecciona "No" o cierra
     * 
     * La confirmación es fundamental para prevenir eliminaciones
     * accidentales en un sistema de gestión municipal donde los
     * eventos pueden tener múltiples dependencias y participantes.
     * 
     * @param evento El evento a eliminar
     * @see EventoService#eliminarEvento(Evento)
     */
    private void eliminarEvento(Evento evento) {
        //Confirmación simple antes de eliminar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("🗑️ Confirmar Eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar este evento?");
        confirmacion.setContentText(null);
        
        // Botones simples: Sí / No
        confirmacion.getButtonTypes().setAll(
            ButtonType.YES, 
            ButtonType.NO
        );
        
        // Mostrar y procesar respuesta
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            // Solo eliminar si presionó "Sí"
            eventoService.eliminarEvento(evento);
            cargarTodosLosEventos(); // Recargar tabla después de eliminar
        }
        // Si presionó "No" o cerró, no hacer nada
    }

}