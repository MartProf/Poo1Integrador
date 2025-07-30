package com.example.controlador;

import com.example.modelo.*;
import com.example.servicio.EventoService;
import com.example.util.BuscarPersonaModalHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la creación y edición de eventos en el sistema municipal.
 * 
 * Esta clase maneja un formulario dinámico que se adapta según el tipo de evento
 * seleccionado (Feria, Concierto, Exposición, Taller, Ciclo de Cine), mostrando
 * campos específicos para cada tipo y gestionando sus validaciones particulares.
 * 
 * Funcionalidades principales:
 * - Creación de nuevos eventos con campos específicos por tipo
 * - Edición de eventos existentes manteniendo su estructura
 * - Gestión de responsables con prevención de duplicados
 * - Integración con modales para selección de personas y películas
 * - Validaciones comprehensivas antes del guardado
 * - Soporte para ambos modos: creación y edición
 * 
 * El controlador opera en dos modalidades:
 * - Modo creación: Formulario limpio para nuevo evento
 * - Modo edición: Formulario pre-cargado con datos del evento existente
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 * @see EventoService
 * @see BuscarPersonaModalHelper
 * @see AgregarPeliculasModalController
 */
public class NuevoEventoController {
    /**
     * Campo de texto para el nombre del evento.
     * 
     * Campo obligatorio que define el título del evento que será
     * visible para los ciudadanos en las listas y búsquedas.
     */
    @FXML
    private TextField txtNombre;

    /**
     * Selector de fecha para el inicio del evento.
     * 
     * Campo obligatorio que establece cuándo comenzará el evento.
     * Se valida que sea una fecha futura al momento de guardado.
     */
    @FXML
    private DatePicker dateFechaInicio;

    /**
     * Campo de texto para la duración en días del evento.
     * 
     * Campo obligatorio que debe contener un número entero positivo
     * indicando cuántos días consecutivos durará el evento.
     */
    @FXML
    private TextField txtDuracion;

    /**
     * ComboBox para seleccionar el tipo de evento.
     * 
     * Determina qué campos específicos se mostrarán en el formulario.
     * Opciones: Feria, Concierto, Exposicion, Taller, CicloDeCine.
     */
    @FXML
    private ComboBox<String> cmbTipoEvento;

    /**
     * ComboBox para seleccionar el estado del evento.
     * 
     * En creación se limita a PLANIFICADO y CONFIRMADO.
     * En edición puede incluir otros estados según el ciclo de vida.
     */
    @FXML
    private ComboBox<EstadoEvento> cmbEstado;

    /**
     * Panel dinámico donde se insertan campos específicos por tipo.
     * 
     * Se limpia y reconstruye cada vez que cambia el tipo de evento,
     * mostrando solo los campos relevantes para el tipo seleccionado.
     */
    @FXML
    private VBox panelCamposEspecificos;

    /**
     * Botón para abrir el modal de selección de responsables.
     * 
     * Permite agregar personas como responsables del evento,
     * con validación de duplicados por DNI.
     */
    @FXML
    private Button btnAgregarResponsable;

    /**
     * Etiqueta que muestra la cantidad de responsables seleccionados.
     * 
     * Se actualiza automáticamente cada vez que se agregan o
     * eliminan responsables de la lista.
     */
    @FXML
    private Label lblCantidadResponsables;

    /**
     * Lista visual de responsables seleccionados.
     * 
     * Muestra nombre, apellido y DNI de cada responsable.
     * Permite eliminación mediante doble click.
     */
    @FXML
    private ListView<Persona> listResponsables;

    /**
     * Etiqueta de subtítulo informativo.
     * 
     * Proporciona contexto adicional sobre el formulario
     * según el modo (creación/edición) y tipo de evento.
     */
    @FXML
    private Label lblSubtitulo;

    /**
     * Campo específico para Feria: cantidad de stands disponibles.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "Feria".
     * Debe contener un número entero positivo.
     */
    private TextField txtCantidadStands;
    
    /**
     * Campo específico para Feria: checkbox para eventos al aire libre.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "Feria".
     * Indica si la feria se realizará en espacios abiertos.
     */
    private CheckBox chkAlAireLibre;

    /**
     * Campo específico para Concierto: checkbox para entrada gratuita.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "Concierto".
     * Determina si el evento tendrá costo de entrada.
     */
    private CheckBox chkEntradaGratuita;

    /**
     * Campo específico para Exposición: tipo de arte exhibido.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "Exposicion".
     * Campo de texto libre para describir el género artístico.
     */
    private TextField txtTipoArte;
    
    /**
     * Botón específico para Exposición: buscar curador responsable.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "Exposicion".
     * Abre modal de selección simple para elegir una persona.
     */
    private Button btnBuscarCurador;
    
    /**
     * Etiqueta específica para Exposición: muestra curador seleccionado.
     * 
     * Se actualiza cuando se selecciona un curador mediante el botón.
     * Muestra nombre y DNI del curador elegido.
     */
    private Label lblCuradorSeleccionado;
    
    /**
     * Referencia específica para Exposición: persona curador seleccionada.
     * 
     * Almacena la instancia de Persona que oficiará como curador.
     * Se valida que no sea null antes del guardado.
     */
    private Persona curadorSeleccionado;

    /**
     * Campo específico para Taller: cupo máximo de participantes.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "Taller".
     * Debe contener un número entero positivo.
     */
    private TextField txtCupoMaximo;
    
    /**
     * Botón específico para Taller: buscar instructor responsable.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "Taller".
     * Abre modal de selección simple para elegir una persona.
     */
    private Button btnBuscarInstructor;
    
    /**
     * Etiqueta específica para Taller: muestra instructor seleccionado.
     * 
     * Se actualiza cuando se selecciona un instructor mediante el botón.
     * Muestra nombre y DNI del instructor elegido.
     */
    private Label lblInstructorSeleccionado;
    
    /**
     * Referencia específica para Taller: persona instructor seleccionada.
     * 
     * Almacena la instancia de Persona que oficiará como instructor.
     * Se valida que no sea null antes del guardado.
     */
    private Persona instructorSeleccionado;

    /**
     * Campo específico para Taller: modalidad de dictado.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "Taller".
     * ComboBox con valores del enum Modalidad (PRESENCIAL, VIRTUAL, HIBRIDA).
     */
    private ComboBox<Modalidad> cmbModalidad;

    /**
     * Campo específico para Ciclo de Cine: checkbox para charlas post-función.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "CicloDeCine".
     * Indica si habrá debates o charlas después de las proyecciones.
     */
    private CheckBox chkHayCharlas;
    
    /**
     * Lista específica para Concierto: artistas participantes.
     * 
     * Se llena mediante modal de selección múltiple.
     * Se valida que tenga al menos un artista antes del guardado.
     */
    private List<Persona> artistasSeleccionados = new ArrayList<>();
    
    /**
     * Botón específico para Concierto: buscar artistas participantes.
     * 
     * Se crea dinámicamente cuando se selecciona tipo "Concierto".
     * Abre modal de selección múltiple para elegir varios artistas.
     */
    private Button btnBuscarArtistas;
    
    /**
     * Etiqueta específica para Concierto: contador de artistas seleccionados.
     * 
     * Se actualiza con la cantidad de artistas en la lista.
     * Proporciona feedback visual sobre la selección actual.
     */
    private Label lblArtistasSeleccionados;

    /**
     * Lista de responsables seleccionados para el evento.
     * 
     * Se utiliza en todos los tipos de evento. La persona logueada
     * se agrega automáticamente si no hay otros responsables.
     * Se previenen duplicados mediante validación por DNI.
     */
    private List<Persona> responsablesSeleccionados = new ArrayList<>();

    /**
     * Servicio para operaciones de persistencia de eventos.
     * 
     * Se inicializa en el constructor y se utiliza para
     * guardar nuevos eventos y actualizar existentes.
     */
    private EventoService eventoService;
    
    /**
     * Persona actualmente autenticada en el sistema.
     * 
     * Se utiliza como responsable por defecto y para
     * validaciones de permisos en operaciones de edición.
     */
    private Persona personaLogueada;

    /**
     * Lista específica para Ciclo de Cine: películas del ciclo.
     * 
     * Se llena mediante modal especializado de gestión de películas.
     * Se valida que tenga al menos una película antes del guardado.
     */
    private List<Pelicula> peliculasAgregadas = new ArrayList<>();
    
    /**
     * Etiqueta específica para Ciclo de Cine: contador de películas.
     * 
     * Se actualiza con la cantidad de películas en la lista.
     * Proporciona feedback visual sobre las películas agregadas.
     */
    private Label lblPeliculasSeleccionadas;

    /**
     * Indica si el controlador está en modo edición o creación.
     * 
     * false: Modo creación - formulario limpio para nuevo evento
     * true: Modo edición - formulario pre-cargado con evento existente
     */
    private boolean modoEdicion = false;
    
    /**
     * Referencia al evento siendo editado.
     * 
     * null en modo creación, contiene el evento a modificar en modo edición.
     * Se utiliza para preservar datos no modificables como ID y fecha de creación.
     */
    private Evento eventoAEditar;

    /**
     * Constructor del controlador de nuevo evento.
     * 
     * Inicializa el servicio de eventos para las operaciones de persistencia.
     * No requiere parámetros ya que la configuración específica se realiza
     * posteriormente mediante los métodos setter correspondientes.
     */
    public NuevoEventoController() {
        this.eventoService = new EventoService();
    }

    /**
     * Inicializa los componentes del formulario después de cargar el FXML.
     * 
     * Configuración realizada:
     * 1. Carga opciones del ComboBox de tipos de evento
     * 2. Configura listener para cambios de tipo de evento
     * 3. Inicializa ComboBox de estados con valores apropiados
     * 4. Establece estado inicial como PLANIFICADO
     * 5. Prepara la lista de responsables vacía
     * 
     * El método se ejecuta automáticamente después de que JavaFX
     * carga el archivo FXML y antes de mostrar la ventana.
     * 
     * @see #handleTipoEventoChanged()
     * @see #actualizarListaResponsables()
     */
    @FXML
    public void initialize() {
        cmbTipoEvento.setItems(FXCollections.observableArrayList(
                "Feria", "Concierto", "Exposicion", "Taller", "CicloDeCine"
        ));
        cmbTipoEvento.setOnAction(e -> handleTipoEventoChanged());

        cmbEstado.setItems(FXCollections.observableArrayList(EstadoEvento.PLANIFICADO, EstadoEvento.CONFIRMADO));
        cmbEstado.getSelectionModel().select(EstadoEvento.PLANIFICADO);
        
        // Inicializar lista de responsables
        actualizarListaResponsables();
        
    }

    /**
     * Maneja la acción de agregar un responsable al evento.
     * 
     * Proceso de agregado:
     * 1. Abre modal de selección simple de personas
     * 2. Valida que se haya seleccionado una persona
     * 3. Verifica que no esté duplicada (comparación por DNI)
     * 4. Agrega a la lista si es válida
     * 5. Actualiza la vista de responsables
     * 6. Muestra mensaje informativo si ya existe
     * 
     * La validación de duplicados utiliza DNI en lugar de equals()
     * para mayor confiabilidad en la comparación de personas.
     * 
     * @see BuscarPersonaModalHelper#abrirSeleccionSimple()
     * @see #actualizarListaResponsables()
     */
    @FXML
    private void handleAgregarResponsable() {
        try {
            // Abrir modal para buscar/agregar persona
            Persona personaSeleccionada = BuscarPersonaModalHelper.abrirSeleccionSimple();
            
            if (personaSeleccionada != null) {
                // Verificar que no esté ya en la lista usando DNI (más confiable que contains)
                boolean yaExiste = responsablesSeleccionados.stream()
                        .anyMatch(persona -> persona.getDni() == personaSeleccionada.getDni());
                
                if (!yaExiste) {
                    responsablesSeleccionados.add(personaSeleccionada);
                    actualizarListaResponsables();
                } else {
                    mostrarAlerta("Información", "Esta persona ya está agregada como responsable.");
                }
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al seleccionar responsable: " + e.getMessage());
        }
    }

    /**
     * Actualiza la visualización de la lista de responsables.
     * 
     * Funcionalidades implementadas:
     * 1. Convierte la lista interna a ObservableList para JavaFX
     * 2. Actualiza el contador en la etiqueta informativa
     * 3. Configura el formato de visualización personalizado (nombre + apellido + DNI)
     * 4. Habilita eliminación por doble click en cualquier elemento
     * 
     * El formato de visualización muestra información completa para
     * identificación inequívoca de cada responsable.
     * 
     * @see #eliminarResponsable(Persona)
     */
    private void actualizarListaResponsables() {
        listResponsables.setItems(FXCollections.observableArrayList(responsablesSeleccionados));
        lblCantidadResponsables.setText(responsablesSeleccionados.size() + " responsable(s) seleccionado(s)");
        
        // Configurar cómo se muestra cada persona en la lista
        listResponsables.setCellFactory(param -> new ListCell<Persona>() {
            @Override
            protected void updateItem(Persona persona, boolean empty) {
                super.updateItem(persona, empty);
                if (empty || persona == null) {
                    setText(null);
                } else {
                    setText(persona.getNombre() + " " + persona.getApellido() + " (DNI: " + persona.getDni() + ")");
                }
            }
        });
        
        // Permitir eliminar con doble click
        listResponsables.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Persona personaSeleccionada = listResponsables.getSelectionModel().getSelectedItem();
                if (personaSeleccionada != null) {
                    eliminarResponsable(personaSeleccionada);
                }
            }
        });
    }
    
    /**
     * Elimina un responsable de la lista de seleccionados.
     * 
     * Operación simple que:
     * 1. Remueve la persona de la lista interna
     * 2. Actualiza la visualización completa
     * 
     * Se invoca mediante doble click en la lista visual.
     * No requiere confirmación adicional dado que la acción
     * es fácilmente reversible agregando nuevamente la persona.
     * 
     * @param persona La persona a eliminar de la lista de responsables
     * @see #actualizarListaResponsables()
     */
    private void eliminarResponsable(Persona persona) {
        responsablesSeleccionados.remove(persona);
        actualizarListaResponsables();
    }

    /**
     * Maneja el cambio en la selección del tipo de evento.
     * 
     * Este método central del controlador es responsable de:
     * 1. Limpiar completamente el panel de campos específicos
     * 2. Evaluar el nuevo tipo seleccionado
     * 3. Invocar el método de construcción correspondiente
     * 4. Reconstruir la interfaz con campos apropiados
     * 
     * Se ejecuta automáticamente cuando el usuario cambia la selección
     * en el ComboBox de tipo de evento, proporcionando una experiencia
     * de formulario dinámico adaptativo.
     * 
     * @see #mostrarCamposFeria()
     * @see #mostrarCamposConcierto()
     * @see #mostrarCamposExposicion()
     * @see #mostrarCamposTaller()
     * @see #mostrarCamposCicloDeCine()
     */
    @FXML
    private void handleTipoEventoChanged() {
        String tipo = cmbTipoEvento.getValue();
        panelCamposEspecificos.getChildren().clear();

        switch (tipo) {
            case "Feria" -> mostrarCamposFeria();
            case "Concierto" -> mostrarCamposConcierto();
            case "Exposicion" -> mostrarCamposExposicion();
            case "Taller" -> mostrarCamposTaller();
            case "CicloDeCine" -> mostrarCamposCicloDeCine();
        }
    }

    /**
     * Construye los campos específicos para eventos tipo Feria.
     * 
     * Campos agregados al panel dinámico:
     * - Campo numérico para cantidad de stands disponibles
     * - Checkbox para indicar si es al aire libre
     * 
     * Estos campos son específicos del modelo Feria y se validan
     * como obligatorios durante el proceso de guardado.
     */
    private void mostrarCamposFeria() {
        txtCantidadStands = new TextField();
        chkAlAireLibre = new CheckBox("Al aire libre");

        panelCamposEspecificos.getChildren().addAll(
                new Label("Cantidad de Stands:"),
                txtCantidadStands,
                chkAlAireLibre
        );
    }

    /**
     * Construye los campos específicos para eventos tipo Concierto.
     * 
     * Campos agregados al panel dinámico:
     * - Botón para selección múltiple de artistas participantes
     * - Etiqueta informativa sobre artistas seleccionados
     * - Checkbox para indicar entrada gratuita
     * 
     * La selección de artistas utiliza modal especializado que permite
     * elegir múltiples personas de la base de datos.
     * 
     * @see BuscarPersonaModalHelper#abrirSeleccionMultiple()
     */
    private void mostrarCamposConcierto() {
        btnBuscarArtistas = new Button("Buscar Artistas");
        lblArtistasSeleccionados = new Label("Ningún artista seleccionado");

        btnBuscarArtistas.setOnAction(e -> {
            List<Persona> encontrados = BuscarPersonaModalHelper.abrirSeleccionMultiple();
            if (encontrados != null) {
                artistasSeleccionados.clear();
                artistasSeleccionados.addAll(encontrados);
                lblArtistasSeleccionados.setText(
                    "Seleccionados: " + artistasSeleccionados.size()
                );
            }
        });

        chkEntradaGratuita = new CheckBox("Entrada gratuita");

        panelCamposEspecificos.getChildren().addAll(
                btnBuscarArtistas,
                lblArtistasSeleccionados,
                chkEntradaGratuita
        );
    }

    /**
     * Construye los campos específicos para eventos tipo Exposición.
     * 
     * Campos agregados al panel dinámico:
     * - Campo de texto libre para tipo de arte
     * - Botón para selección simple de curador responsable
     * - Etiqueta informativa sobre curador seleccionado
     * 
     * El curador es obligatorio para exposiciones y debe ser una
     * persona registrada en el sistema.
     * 
     * @see BuscarPersonaModalHelper#abrirSeleccionSimple()
     */
    private void mostrarCamposExposicion() {
        txtTipoArte = new TextField();
        btnBuscarCurador = new Button("Buscar Curador");
        lblCuradorSeleccionado = new Label("Ningún curador seleccionado");

        btnBuscarCurador.setOnAction(e -> {
            Persona p = BuscarPersonaModalHelper.abrirSeleccionSimple();
            if (p != null) {
                curadorSeleccionado = p;
                lblCuradorSeleccionado.setText(p.getNombre() + " (" + p.getDni() + ")");
            }
        });

        panelCamposEspecificos.getChildren().addAll(
                new Label("Tipo de Arte:"),
                txtTipoArte,
                btnBuscarCurador,
                lblCuradorSeleccionado
        );
    }

    /**
     * Construye los campos específicos para eventos tipo Taller.
     * 
     * Campos agregados al panel dinámico:
     * - Campo numérico para cupo máximo de participantes
     * - Botón para selección simple de instructor responsable
     * - Etiqueta informativa sobre instructor seleccionado
     * - ComboBox para modalidad de dictado (presencial/virtual/híbrida)
     * 
     * Todos los campos son obligatorios para talleres, implementando
     * la interfaz TieneCupo para gestión de inscripciones.
     * 
     * @see Modalidad
     * @see TieneCupo
     * @see BuscarPersonaModalHelper#abrirSeleccionSimple()
     */
    private void mostrarCamposTaller() {
        txtCupoMaximo = new TextField();
        btnBuscarInstructor = new Button("Buscar Instructor");
        lblInstructorSeleccionado = new Label("Ningún instructor seleccionado");
        cmbModalidad = new ComboBox<>(FXCollections.observableArrayList(Modalidad.values()));

        btnBuscarInstructor.setOnAction(e -> {
            Persona p = BuscarPersonaModalHelper.abrirSeleccionSimple();
            if (p != null) {
                instructorSeleccionado = p;
                lblInstructorSeleccionado.setText(p.getNombre() + " (" + p.getDni() + ")");
            }
        });

        panelCamposEspecificos.getChildren().addAll(
                new Label("Cupo máximo:"),
                txtCupoMaximo,
                btnBuscarInstructor,
                lblInstructorSeleccionado,
                new Label("Modalidad:"),
                cmbModalidad
        );
    }

    /**
     * Construye los campos específicos para eventos tipo Ciclo de Cine.
     * 
     * Campos agregados al panel dinámico:
     * - Checkbox para indicar si incluye charlas posteriores
     * - Botón para abrir modal especializado de gestión de películas
     * - Etiqueta informativa sobre cantidad de películas seleccionadas
     * 
     * El modal de películas permite agregar/editar la información
     * cinematográfica específica del ciclo.
     * 
     * @see #handleAgregarPeliculas()
     * @see AgregarPeliculasModalController
     */
    private void mostrarCamposCicloDeCine() {
        chkHayCharlas = new CheckBox("¿Incluye charlas posteriores?");
        Button btnAgregarPeliculas = new Button("Agregar Películas");
        lblPeliculasSeleccionadas = new Label("0 películas seleccionadas");

        btnAgregarPeliculas.setOnAction(e -> {
            try {
                handleAgregarPeliculas();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        panelCamposEspecificos.getChildren().addAll(
            chkHayCharlas,
            btnAgregarPeliculas,
            lblPeliculasSeleccionadas
        );
    }

    /**
     * Maneja el proceso completo de guardado/actualización de eventos.
     * 
     * Este método central implementa el flujo completo de validación y persistencia:
     * 
     * FASE 1 - Validaciones Generales:
     * - Verifica que el nombre no esté vacío
     * - Valida que la fecha de inicio esté seleccionada
     * - Confirma que la duración sea un número positivo
     * - Asegura que se haya seleccionado un estado
     * - Verifica que se haya elegido un tipo de evento
     * 
     * FASE 2 - Creación/Reutilización de Instancia:
     * - En modo edición: reutiliza el evento existente
     * - En modo creación: instancia el tipo específico según selección
     * 
     * FASE 3 - Asignación de Campos Comunes:
     * - Aplica valores validados a la instancia base
     * - Configura responsables según modo de operación
     * 
     * FASE 4 - Validaciones y Configuración Específica:
     * - Ejecuta validaciones particulares por tipo de evento
     * - Configura campos específicos según el tipo seleccionado
     * 
     * FASE 5 - Persistencia:
     * - Invoca servicio de guardado o actualización según modo
     * - Limpia formulario en caso de éxito
     * 
     * El método maneja todas las excepciones posibles mostrando
     * mensajes específicos para validación y errores generales.
     * 
     * @see EventoService#guardarEvento(Evento)
     * @see EventoService#actualizarEvento(Evento)
     * @see #mantenerResponsableEvento(Evento)
     * @see #limpiarCampos()
     */
    @FXML
    public void handleGuardarEvento() {
        try {
            // Validaciones generales
            String nombre = txtNombre.getText();
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del evento no puede estar vacío.");
            }

            LocalDate fechaInicio = dateFechaInicio.getValue();
            if (fechaInicio == null) {
                throw new IllegalArgumentException("Debe seleccionar una fecha de inicio.");
            }

            int duracion;
            try {
                duracion = Integer.parseInt(txtDuracion.getText());
                if (duracion <= 0) {
                    throw new IllegalArgumentException("La duración debe ser un número positivo.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("La duración debe ser un número válido.");
            }

            EstadoEvento estado = cmbEstado.getValue();
            if (estado == null) {
                throw new IllegalArgumentException("Debe seleccionar un estado para el evento.");
            }

            String tipo = cmbTipoEvento.getValue();
            if (tipo == null || tipo.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de evento.");
            }

            Evento evento;
            if (modoEdicion && eventoAEditar != null) {
                evento = eventoAEditar;
            } else {
                switch (tipo) {
                    case "Feria" -> evento = new Feria();
                    case "Concierto" -> evento = new Concierto();
                    case "Exposicion" -> evento = new Exposicion();
                    case "Taller" -> evento = new Taller();
                    case "CicloDeCine" -> evento = new CicloDeCine();
                    default -> throw new IllegalStateException("Tipo de evento no soportado");
                }
            }

            evento.setNombre(nombre);
            evento.setFechaInicio(fechaInicio);
            evento.setDuraciónDias(duracion);
            evento.setEstado(estado);
            mantenerResponsableEvento(evento);

            // Validaciones y asignaciones específicas por tipo
            switch (tipo) {
                case "Feria" -> {
                    int cantidad;
                    try {
                        cantidad = Integer.parseInt(txtCantidadStands.getText());
                        if (cantidad <= 0) {
                            throw new IllegalArgumentException("La cantidad de stands debe ser un número positivo.");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("La cantidad de stands debe ser un número válido.");
                    }
                    Feria feria = (Feria) evento;
                    feria.setCantidadDeStand(cantidad);
                    feria.setAlAirelibre(chkAlAireLibre.isSelected());
                }

                case "Concierto" -> {
                    if (artistasSeleccionados == null || artistasSeleccionados.isEmpty()) {
                        throw new IllegalArgumentException("Debe agregar al menos un artista al concierto.");
                    }
                    Concierto concierto = (Concierto) evento;
                    concierto.setArtistas(artistasSeleccionados);
                    concierto.setEntradaGratuita(chkEntradaGratuita.isSelected());
                }

                case "Exposicion" -> {
                    String tipoArte = txtTipoArte.getText();
                    if (tipoArte == null || tipoArte.trim().isEmpty()) {
                        throw new IllegalArgumentException("Debe ingresar el tipo de arte.");
                    }
                    if (curadorSeleccionado == null) {
                        throw new IllegalArgumentException("Debe seleccionar un curador.");
                    }
                    Exposicion exposicion = (Exposicion) evento;
                    exposicion.setTipoArte(tipoArte);
                    exposicion.setCurador(curadorSeleccionado);
                }

                case "Taller" -> {
                    int cupo;
                    try {
                        cupo = Integer.parseInt(txtCupoMaximo.getText());
                        if (cupo <= 0) {
                            throw new IllegalArgumentException("El cupo máximo debe ser un número positivo.");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("El cupo máximo debe ser un número válido.");
                    }
                    if (instructorSeleccionado == null) {
                        throw new IllegalArgumentException("Debe seleccionar un instructor.");
                    }
                    if (cmbModalidad.getValue() == null) {
                        throw new IllegalArgumentException("Debe seleccionar una modalidad.");
                    }
                    Taller taller = (Taller) evento;
                    taller.setCupoMaximo(cupo);
                    taller.setInstructor(instructorSeleccionado);
                    taller.setModalidad(cmbModalidad.getValue());
                }

                case "CicloDeCine" -> {
                    if (peliculasAgregadas == null || peliculasAgregadas.isEmpty()) {
                        throw new IllegalArgumentException("Debe agregar al menos una película al ciclo de cine.");
                    }
                    CicloDeCine ciclo = (CicloDeCine) evento;
                    ciclo.setHayCharlas(chkHayCharlas.isSelected());
                    ciclo.setPeliculas(peliculasAgregadas);
                    for (Pelicula pelicula : peliculasAgregadas) {
                        pelicula.setCicloDeCine(ciclo);
                    }
                }
            }

            // Guardar o actualizar según el modo de operación
            if (modoEdicion) {
                eventoService.actualizarEvento(evento);
            } else {
                eventoService.guardarEvento(evento);
            }

            // Limpiar formulario después del éxito
            limpiarCampos();

        } catch (IllegalArgumentException e) {
            mostrarAlerta("Error de validación", e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "Error al guardar/actualizar evento: " + ex.getMessage());
        }
    }


    /**
     * Abre el modal especializado para gestión de películas en ciclos de cine.
     * 
     * Funcionalidad del modal:
     * 1. Carga el FXML del controlador de películas
     * 2. Configura la referencia al controlador padre (este)
     * 3. Si hay películas previamente seleccionadas, las carga en el modal
     * 4. Presenta modal con bloqueo de aplicación hasta cierre
     * 
     * El modal permite agregar/editar/eliminar películas del ciclo,
     * manteniendo la lista sincronizada con este controlador mediante
     * el método callback setPeliculasAgregadas().
     * 
     * @throws IOException si hay error cargando el archivo FXML del modal
     * @see AgregarPeliculasModalController
     * @see #setPeliculasAgregadas(List)
     */
    @FXML
    private void handleAgregarPeliculas() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ventanaAgregarPeliculas.fxml"));
        Parent root = loader.load();

        AgregarPeliculasModalController controller = loader.getController();
        controller.setControladorPadre(this);
        
        // Cargar las películas existentes en el modal
        if (peliculasAgregadas != null && !peliculasAgregadas.isEmpty()) {
            ObservableList<Pelicula> peliculasObservable = FXCollections.observableArrayList(peliculasAgregadas);
            controller.cargarPeliculasExistentes(peliculasObservable);
        }

        Stage stage = new Stage();
        stage.setTitle("Agregar Películas");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    /**
     * Gestiona la asignación de responsables según el modo de operación.
     * 
     * Lógica de responsables:
     * 
     * MODO CREACIÓN:
     * - Si no hay responsables seleccionados manualmente
     * - Agrega automáticamente la persona logueada como responsable mínimo
     * - Asigna la lista completa al evento
     * 
     * MODO EDICIÓN:
     * - Utiliza la lista actual de responsables seleccionados
     * - Garantiza que la persona logueada esté incluida (requisito de permisos)
     * - Mantiene otros responsables previamente asignados
     * 
     * Esta gestión asegura que siempre haya al menos un responsable
     * y que quien edita el evento tenga responsabilidad sobre él.
     * 
     * @param evento El evento al cual asignar los responsables
     * @see #actualizarListaResponsables()
     */
    private void mantenerResponsableEvento(Evento evento) {
        if (!modoEdicion) {
            // En creación, usar la lista de responsables seleccionados
            if (responsablesSeleccionados.isEmpty()) {
                // Si no hay responsables seleccionados, agregar la persona logueada como mínimo
                responsablesSeleccionados.add(personaLogueada);
                actualizarListaResponsables();
            }
            evento.setResponsables(FXCollections.observableArrayList(responsablesSeleccionados));
        } else {
            // En edición, usar la lista actual y asegurar que la persona logueada esté incluida
            evento.setResponsables(FXCollections.observableArrayList(responsablesSeleccionados));
            if (!evento.getResponsables().contains(personaLogueada)) {
                evento.getResponsables().add(personaLogueada);
            }
        }
    }
    
    /**
     * Limpia todos los campos del formulario para nueva creación.
     * 
     * Operaciones de limpieza:
     * 1. Vacía campos de texto comunes (nombre, duración)
     * 2. Resetea selectores de fecha
     * 3. Restaura estado inicial (PLANIFICADO)
     * 4. Limpia selección de tipo de evento
     * 5. Vacía panel de campos específicos
     * 6. Limpia lista de responsables seleccionados
     * 
     * Se invoca automáticamente después de guardado exitoso
     * o puede usarse para preparar nueva creación.
     */
    private void limpiarCampos() {
        txtNombre.clear();
        dateFechaInicio.setValue(null);
        txtDuracion.clear();
        cmbEstado.getSelectionModel().select(EstadoEvento.PLANIFICADO);
        cmbTipoEvento.getSelectionModel().clearSelection();
        panelCamposEspecificos.getChildren().clear();
        
        // Limpiar lista de responsables
        responsablesSeleccionados.clear();
        actualizarListaResponsables();
    }

    /**
     * Configura la persona actualmente autenticada en el sistema.
     * 
     * Esta persona se utiliza para:
     * - Agregarse automáticamente como responsable si no hay otros
     * - Validaciones de permisos en modo edición
     * - Auditoría de operaciones de guardado
     * 
     * Debe invocarse antes de mostrar el formulario para asegurar
     * el funcionamiento correcto de las operaciones de responsabilidad.
     * 
     * @param persona La persona autenticada que utilizará el formulario
     */
    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
    }

    /**
     * Actualiza la lista de películas del ciclo de cine.
     * 
     * Método callback invocado por el modal de gestión de películas
     * para sincronizar la lista entre el modal y este controlador.
     * 
     * Funcionalidad:
     * 1. Actualiza la lista interna de películas
     * 2. Refresca la etiqueta informativa con la nueva cantidad
     * 3. Mantiene sincronización entre modal y formulario principal
     * 
     * @param peliculas Lista actualizada de películas del ciclo
     * @see AgregarPeliculasModalController
     */
    public void setPeliculasAgregadas(List<Pelicula> peliculas) {
        if (peliculas != null) {
            this.peliculasAgregadas = peliculas;
            if (lblPeliculasSeleccionadas != null) {
                lblPeliculasSeleccionadas.setText(peliculas.size() + " película(s) seleccionada(s)");
            }
        }
    }
    
    /**
     * Configura el controlador para operar en modo edición.
     * 
     * El modo edición modifica el comportamiento del controlador:
     * - Cambia la lógica de gestión de responsables
     * - Altera el método de persistencia (actualizar vs crear)
     * - Modifica validaciones según el contexto de edición
     * 
     * @param modoEdicion true para modo edición, false para modo creación
     * @see #cargarEventoParaEditar(Evento)
     */
    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    /**
     * Carga un evento existente en el formulario para edición.
     * 
     * Este método complejo realiza la reconstrucción completa del formulario:
     * 
     * PREPARACIÓN INICIAL:
     * - Almacena referencia al evento a editar
     * - Activa modo edición del controlador
     * 
     * CAMPOS COMUNES:
     * - Restaura nombre, fecha de inicio, duración y estado
     * - Establece valores en todos los controles generales
     * 
     * DETECCIÓN DE TIPO Y RECONSTRUCCIÓN:
     * - Identifica el tipo específico mediante instanceof
     * - Selecciona el tipo en el ComboBox correspondiente
     * - Dispara reconstrucción de campos específicos
     * - Restaura valores particulares de cada tipo
     * 
     * CASOS ESPECÍFICOS POR TIPO:
     * - Feria: cantidad de stands y ubicación
     * - Concierto: lista de artistas y política de entrada
     * - Exposición: tipo de arte y curador responsable
     * - Taller: cupo, instructor y modalidad
     * - Ciclo de Cine: charlas y lista de películas
     * 
     * RESTAURACIÓN DE RESPONSABLES:
     * - Transfiere responsables existentes a la lista de trabajo
     * - Actualiza visualización de responsables
     * - Mantiene integridad de permisos de edición
     * 
     * La reconstrucción es exhaustiva para asegurar que el formulario
     * refleje exactamente el estado del evento persistido.
     * 
     * @param evento El evento existente a cargar para edición
     * @see #handleTipoEventoChanged()
     * @see #actualizarListaResponsables()
     */
    public void cargarEventoParaEditar(Evento evento) {
        this.eventoAEditar = evento;
        this.modoEdicion = true;

        // === CAMPOS COMUNES ===
        txtNombre.setText(evento.getNombre());
        dateFechaInicio.setValue(evento.getFechaInicio());
        txtDuracion.setText(String.valueOf(evento.getDuraciónDias()));
        cmbEstado.setValue(evento.getEstado());

        // === CAMPOS ESPECIFICOS ===
        String tipo;
        if (evento instanceof Feria feria) {
            tipo = "Feria";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            txtCantidadStands.setText(String.valueOf(feria.getCantidadDeStand()));
            chkAlAireLibre.setSelected(feria.isAlAirelibre());

        } else if (evento instanceof Concierto concierto) {
            tipo = "Concierto";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            artistasSeleccionados.clear();
            artistasSeleccionados.addAll(concierto.getArtistas());
            lblArtistasSeleccionados.setText("Seleccionados: " + artistasSeleccionados.size());
            chkEntradaGratuita.setSelected(concierto.isEntradaGratuita());

        } else if (evento instanceof Exposicion exposicion) {
            tipo = "Exposicion";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            txtTipoArte.setText(exposicion.getTipoArte());
            curadorSeleccionado = exposicion.getCurador();
            lblCuradorSeleccionado.setText(curadorSeleccionado != null
                ? curadorSeleccionado.getNombre() + " (" + curadorSeleccionado.getDni() + ")"
                : "Ningún curador seleccionado");

        } else if (evento instanceof Taller taller) {
            tipo = "Taller";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            txtCupoMaximo.setText(String.valueOf(taller.getCupoMaximo()));
            instructorSeleccionado = taller.getInstructor();
            lblInstructorSeleccionado.setText(instructorSeleccionado != null
                ? instructorSeleccionado.getNombre() + " (" + instructorSeleccionado.getDni() + ")"
                : "Ningún instructor seleccionado");
            cmbModalidad.setValue(taller.getModalidad());

        } else if (evento instanceof CicloDeCine ciclo) {
            tipo = "CicloDeCine";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            chkHayCharlas.setSelected(ciclo.isHayCharlas());
            peliculasAgregadas = ciclo.getPeliculas();
            lblPeliculasSeleccionadas.setText(peliculasAgregadas.size() + " película(s) seleccionada(s)");
        }
        
        // === CARGAR RESPONSABLES ===
        // Este paso es crucial para el modo edición
        if (evento.getResponsables() != null && !evento.getResponsables().isEmpty()) {
            responsablesSeleccionados.clear();
            responsablesSeleccionados.addAll(evento.getResponsables());
            actualizarListaResponsables();
        }
    }

    /**
     * Muestra ventanas de alerta para errores y mensajes informativos.
     * 
     * Utiliza AlertType.ERROR para mantener consistencia visual
     * en toda la aplicación. Configura ventana modal simple con
     * título personalizable y mensaje específico.
     * 
     * Se utiliza tanto para errores de validación como para
     * errores de persistencia y comunicación con servicios.
     * 
     * @param titulo El título de la ventana de alerta
     * @param mensaje El contenido del mensaje a mostrar al usuario
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
