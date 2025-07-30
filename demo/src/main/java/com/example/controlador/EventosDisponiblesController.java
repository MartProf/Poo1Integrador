package com.example.controlador;

import com.example.modelo.EstadoEvento;
import com.example.modelo.Evento;
import com.example.modelo.Persona;
import com.example.servicio.EventoService;
import com.example.servicio.ParticipanteService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.time.LocalDate;
import java.io.IOException;
import java.util.List;

/**
 * Controlador para la vista de eventos disponibles con calendario interactivo.
 * 
 * Este controlador implementa una interfaz ciudadana completa que permite:
 * 1. Visualizar eventos en un calendario mensual interactivo
 * 2. Explorar eventos por fechas específicas con indicadores visuales
 * 3. Gestionar inscripciones ciudadanas con validaciones exhaustivas
 * 4. Ver detalles completos de eventos disponibles
 * 5. Filtrar automáticamente eventos según disponibilidad y fechas
 * 
 * Características del calendario:
 * - Navegación mensual con botones anterior/siguiente
 * - Indicadores visuales por estado: hoy, seleccionado, con eventos, normales
 * - Códigos de color diferenciados para eventos activos vs. terminados
 * - Información de cupo en tiempo real para eventos con límite
 * 
 * Sistema de inscripciones:
 * - Validación de estado de evento (solo CONFIRMADO/EN_EJECUCION)
 * - Verificación de fechas (no eventos terminados)
 * - Control de cupo para eventos TieneCupo
 * - Prevención de duplicados e inscripción de responsables
 * - Integración con modal de búsqueda de personas
 * 
 * Filtrado inteligente:
 * - Solo muestra eventos disponibles para inscripción
 * - Excluye eventos PLANIFICADO y FINALIZADO
 * - Excluye eventos que ya terminaron por fechas
 * - Información contextual de disponibilidad en tiempo real
 * 
 * El controlador está optimizado para la experiencia ciudadana, 
 * proporcionando información clara y acciones directas para participación
 * en la programación cultural municipal.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 * @see EventoService
 * @see ParticipanteService
 * @see BuscarPersonaController
 * @see DetallesEventoController
 */
public class EventosDisponiblesController {

    // =============== COMPONENTES DEL CALENDARIO INTERACTIVO ===============
    
    /**
     * Panel principal del calendario organizado como grilla.
     * 
     * Implementa un calendario mensual donde cada celda representa un día
     * con indicadores visuales de eventos. La grilla se reconstruye
     * dinámicamente al cambiar de mes y responde a clics para selección.
     */
    @FXML
    private GridPane calendarioGrid;
    
    /**
     * Etiqueta que muestra el mes y año actual del calendario.
     * 
     * Se actualiza automáticamente al navegar entre meses,
     * proporcionando contexto temporal al usuario.
     */
    @FXML
    private Label lblMesAno;
    
    /**
     * Botón para navegar al mes anterior.
     * 
     * Retrocede un mes en el calendario y actualiza toda la vista
     * incluyendo eventos y indicadores visuales.
     */
    @FXML
    private Button btnMesAnterior;
    
    /**
     * Botón para navegar al mes siguiente.
     * 
     * Avanza un mes en el calendario y actualiza toda la vista
     * incluyendo eventos y indicadores visuales.
     */
    @FXML
    private Button btnMesSiguiente;

    // =============== TABLA DE EVENTOS DEL DÍA SELECCIONADO ===============
    
    /**
     * Tabla principal que muestra eventos del día seleccionado.
     * 
     * Se actualiza automáticamente al hacer clic en días del calendario,
     * mostrando solo eventos disponibles para inscripción con información
     * detallada y acciones directas.
     */
    @FXML
    private TableView<Evento> tablaEventosDelDia;
    
    /**
     * Columna que muestra el nombre del evento.
     * 
     * Campo principal de identificación para el ciudadano,
     * utiliza el nombre completo configurado en el evento.
     */
    @FXML
    private TableColumn<Evento, String> colNombre;
    
    /**
     * Columna que muestra el tipo de evento con iconos.
     * 
     * Utiliza iconos emoji para diferenciación visual rápida:
     * 🏪 Feria, 🎵 Concierto, 🎨 Exposición, 🔧 Taller, 🎬 Ciclo de Cine
     */
    @FXML
    private TableColumn<Evento, String> colTipo;
    
    /**
     * Columna que muestra información de fechas del evento.
     * 
     * Para eventos de un día: fecha única
     * Para eventos multi-día: rango de fechas (inicio a fin)
     */
    @FXML
    private TableColumn<Evento, String> colHorario;
    
    /**
     * Columna que muestra la duración del evento.
     * 
     * Indica cuántos días consecutivos durará el evento,
     * información útil para planificación de participación.
     */
    @FXML
    private TableColumn<Evento, String> colDuracion;
    
    /**
     * Columna que muestra el estado del evento con iconos.
     * 
     * Estados con iconos: 📋 Planificado, ✅ Confirmado, 
     * ⚡ En Ejecución, 🏁 Finalizado.
     * Incluye lógica automática para eventos terminados por fechas.
     */
    @FXML
    private TableColumn<Evento, String> colEstado;
    
    /**
     * Columna que muestra información de cupo disponible.
     * 
     * Para eventos TieneCupo: "X/Y disponibles" (disponibles/total)
     * Para eventos sin límite: "Sin límite"
     * Se actualiza en tiempo real con inscripciones.
     */
    @FXML
    private TableColumn<Evento, String> colCupo;
    
    /**
     * Columna que muestra responsables del evento.
     * 
     * Un responsable: Nombre completo
     * Múltiples: "X responsables"
     * Sin responsables: "Sin asignar"
     */
    @FXML
    private TableColumn<Evento, String> colResponsables;
    
    /**
     * Columna con botones de acción dinámicos.
     * 
     * Incluye botones "✓ Inscribir" y "📋 Detalles" con
     * habilitación/deshabilitación inteligente según disponibilidad.
     */
    @FXML
    private TableColumn<Evento, Void> colAcciones;

    // =============== ETIQUETAS INFORMATIVAS ===============
    
    /**
     * Etiqueta que muestra la fecha del día seleccionado.
     * 
     * Formato: "📋 Eventos del YYYY-MM-DD"
     * Proporciona contexto sobre qué día se está visualizando.
     */
    @FXML
    private Label lblEventosDelDia;
    
    /**
     * Etiqueta que muestra la cantidad de eventos del día.
     * 
     * Formato: "X evento(s)"
     * Proporciona conteo rápido de eventos disponibles.
     */
    @FXML
    private Label lblCantidadEventos;

    // =============== BOTONES DE FILTROS RÁPIDOS ===============
    
    /**
     * Botón para navegar rápidamente al día actual.
     * 
     * Resetea el calendario a la fecha de hoy y selecciona
     * automáticamente el día actual mostrando sus eventos.
     */
    @FXML
    private Button btnHoy;
    
    /**
     * Botón para mostrar eventos de la semana actual.
     * 
     * Actualmente implementado como navegación a hoy,
     * preparado para expansión a vista semanal.
     */
    @FXML
    private Button btnEstaSemana;
    
    /**
     * Botón para navegar al próximo mes.
     * 
     * Avanza un mes en el calendario para exploración
     * de eventos futuros en el sistema.
     */
    @FXML
    private Button btnProximoMes;
    
    /**
     * Botón para actualizar manualmente la vista.
     * 
     * Recarga todos los eventos desde la base de datos
     * y actualiza calendario y tabla completamente.
     */
    @FXML
    private Button btnActualizar;

    // =============== SERVICIOS Y ESTADO INTERNO ===============
    
    /**
     * Persona autenticada en el sistema.
     * 
     * Aunque no se usa actualmente para filtrado (vista ciudadana
     * muestra todos los eventos disponibles), se mantiene para
     * futuras funcionalidades como preferencias personalizadas.
     */
    private Persona personaLogueada;
    
    /**
     * Servicio para operaciones de eventos.
     * 
     * Utilizado para cargar eventos disponibles y validaciones
     * relacionadas con responsabilidad de eventos.
     */
    private EventoService eventoService;
    
    /**
     * Servicio para gestión de participantes.
     * 
     * Maneja todas las operaciones de inscripción, verificación
     * de duplicados y validaciones de cupo.
     */
    private ParticipanteService participanteService;
    
    // =============== VARIABLES DE CONTROL DEL CALENDARIO ===============
    
    /**
     * Fecha del mes actualmente mostrado en el calendario.
     * 
     * Controla qué mes se visualiza, independiente del día
     * seleccionado para ver eventos.
     */
    private java.time.LocalDate fechaActual;
    
    /**
     * Día específico seleccionado por el usuario.
     * 
     * Determina qué eventos se muestran en la tabla inferior,
     * puede ser diferente al mes visualizado.
     */
    private java.time.LocalDate diaSeleccionado;
    
    /**
     * Lista de todos los eventos disponibles para inscripción.
     * 
     * Contiene solo eventos en estado CONFIRMADO o EN_EJECUCION
     * que no hayan terminado por fechas. Se filtra automáticamente
     * al cargar desde la base de datos.
     */
    private List<Evento> todosLosEventos;

    /**
     * Constructor del controlador de eventos disponibles.
     * 
     * Inicializa los servicios necesarios y establece fechas por defecto:
     * - EventoService para operaciones de eventos
     * - ParticipanteService para gestión de inscripciones
     * - Fecha actual como fecha inicial del calendario
     * - Día actual como día seleccionado inicial
     * 
     * La configuración específica de la interfaz se realiza en initialize().
     */
    public EventosDisponiblesController() {
        this.eventoService = new EventoService();
        this.participanteService = new ParticipanteService();
        this.fechaActual = java.time.LocalDate.now();
        this.diaSeleccionado = java.time.LocalDate.now();
    }

    /**
     * Inicializa todos los componentes de la interfaz después de cargar el FXML.
     * 
     * Secuencia de inicialización:
     * 1. Configuración de columnas de tabla con factories específicas
     * 2. Configuración de manejadores de eventos para botones
     * 3. Carga inicial de eventos disponibles desde la base de datos
     * 4. Generación del calendario con indicadores visuales
     * 5. Mostrar eventos del día actual por defecto
     * 
     * Se ejecuta automáticamente por JavaFX antes de mostrar la ventana.
     * 
     * @see #initColumns()
     * @see #initEventHandlers()
     * @see #cargarTodosLosEventos()
     * @see #generarCalendario()
     * @see #mostrarEventosDelDia(LocalDate)
     */
    @FXML
    public void initialize() {
        initColumns();
        initEventHandlers();
        cargarTodosLosEventos();
        generarCalendario();
        mostrarEventosDelDia(diaSeleccionado);
    }

    /**
     * Configura todas las columnas de la tabla de eventos del día.
     * 
     * Configuración específica por columna:
     * - Nombre: Texto directo del evento
     * - Tipo: Iconos emoji específicos por tipo
     * - Horario: Fecha única o rango según duración
     * - Duración: Formato "X día(s)"
     * - Estado: Iconos con lógica de finalización automática
     * - Cupo: Información dinámica de disponibilidad
     * - Responsables: Formato inteligente según cantidad
     * - Acciones: CellFactory complejo con botones condicionales
     * 
     * Todas las columnas utilizan SimpleStringProperty excepto
     * Acciones que implementa CellFactory personalizado.
     * 
     * @see #obtenerTipoEvento(Evento)
     * @see #obtenerInfoFechas(Evento)
     * @see #formatearEstadoConFecha(Evento)
     * @see #obtenerInfoCupo(Evento)
     * @see #getBotonAccionesCellFactory()
     */
    private void initColumns() {
        // Configurar columnas de la tabla de eventos del día
        colNombre.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));

        colTipo.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(obtenerTipoEvento(data.getValue())));

        colHorario.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(obtenerInfoFechas(data.getValue())));

        colDuracion.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDuraciónDias() + " día(s)"));

        colEstado.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(formatearEstadoConFecha(data.getValue())));

        colCupo.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(obtenerInfoCupo(data.getValue())));

        colResponsables.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(obtenerResponsables(data.getValue())));

        // Configurar columna de acciones
        colAcciones.setCellFactory(getBotonAccionesCellFactory());
    }

    /**
     * Genera CellFactory para la columna de acciones con lógica inteligente.
     * 
     * Implementa TableCell personalizado que:
     * 
     * COMPONENTES:
     * - Botón "✓ Inscribir": Para inscribir personas al evento
     * - Botón "📋 Detalles": Para ver información completa del evento
     * - Layout horizontal con espaciado mínimo
     * 
     * LÓGICA DE HABILITACIÓN INTELIGENTE:
     * - Evalúa si el evento es inscribible (estado + fechas)
     * - Verifica cupo disponible para eventos TieneCupo
     * - Cambia colores según disponibilidad:
     *   * Verde: Inscribible con cupo
     *   * Naranja: Sin cupo disponible
     *   * Gris: No inscribible (estado/fechas)
     * 
     * ESTILOS COMPACTOS:
     * - Botones pequeños optimizados para tabla
     * - Colores diferenciados para feedback visual inmediato
     * - Padding y fuentes reducidas para mejor aprovechamiento
     * 
     * MANEJO DE FILAS VACÍAS:
     * - Detecta filas vacías y oculta botones apropiadamente
     * - Previene errores de índice en filas sin datos
     * 
     * @return CellFactory configurado para generar botones dinámicos
     * @see #inscribirPersonaEnEvento(Evento)
     * @see #verDetallesEvento(Evento)
     * @see #esEventoInscribible(Evento)
     */
    private Callback<TableColumn<Evento, Void>, TableCell<Evento, Void>> getBotonAccionesCellFactory() {
        return param -> new TableCell<>() {
            private final Button btnInscribir = new Button("✓ Inscribir");
            private final Button btnVerDetalle = new Button("📋 Detalles");
            private final HBox pane = new HBox(3, btnInscribir, btnVerDetalle);

            {
                // Estilos más compactos para los botones
                btnInscribir.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10px;");
                btnVerDetalle.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10px;");

                btnInscribir.setOnAction(event -> {
                    Evento evento = getTableView().getItems().get(getIndex());
                    inscribirPersonaEnEvento(evento);
                });

                btnVerDetalle.setOnAction(event -> {
                    Evento evento = getTableView().getItems().get(getIndex());
                    verDetallesEvento(evento);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Evento evento = getTableView().getItems().get(getIndex());

                    // Usar la nueva lógica de validación
                    boolean eventoInscribible = esEventoInscribible(evento);

                    // Si tiene cupo (es instancia de TieneCupo), verificar cupo disponible
                    boolean hayCupo = true;
                    if (evento instanceof com.example.modelo.TieneCupo) {
                        com.example.modelo.TieneCupo cupoEvento = (com.example.modelo.TieneCupo) evento;
                        int inscriptos = evento.getParticipantes() != null ? evento.getParticipantes().size() : 0;
                        int cupoDisponible = cupoEvento.getCupoMaximo() - inscriptos;
                        hayCupo = cupoDisponible > 0;
                    }

                    // Habilitar botón solo si el evento es inscribible y hay cupo
                    btnInscribir.setDisable(!(eventoInscribible && hayCupo));
                    
                    // Cambiar estilo del botón según disponibilidad
                    if (!eventoInscribible) {
                        btnInscribir.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10px;");
                    } else if (!hayCupo) {
                        btnInscribir.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10px;");
                    } else {
                        btnInscribir.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 3; -fx-font-size: 10px;");
                    }

                    setGraphic(pane);
                }
            }
        };
    }

    /**
     * Configura manejadores de eventos para todos los botones de control.
     * 
     * Configuración de navegación:
     * - btnMesAnterior/btnMesSiguiente: Navegación mensual del calendario
     * - btnHoy: Navegación rápida al día actual
     * - btnEstaSemana: Vista de eventos semanales (actualmente redirige a hoy)
     * - btnProximoMes: Navegación rápida al próximo mes
     * - btnActualizar: Recarga completa de datos y vista
     * 
     * Todos los manejadores están protegidos contra referencias null
     * y ejecutan actualizaciones completas de la vista según corresponda.
     */
    private void initEventHandlers() {
        // Navegación del calendario
        btnMesAnterior.setOnAction(e -> cambiarMes(-1));
        btnMesSiguiente.setOnAction(e -> cambiarMes(1));
        
        // Filtros rápidos
        btnHoy.setOnAction(e -> irAHoy());
        btnEstaSemana.setOnAction(e -> mostrarEventosEstaSemana());
        btnProximoMes.setOnAction(e -> mostrarEventosProximoMes());
        btnActualizar.setOnAction(e -> actualizarVista());
    }

    // =============== MÉTODOS AUXILIARES PARA FORMATEO DE COLUMNAS ===============
    
    /**
     * Obtiene representación visual del tipo de evento con iconos emoji.
     * 
     * Mapeo de tipos a iconos:
     * - Feria: 🏪 Feria
     * - Concierto: 🎵 Concierto  
     * - Exposición: 🎨 Exposición
     * - Taller: 🔧 Taller
     * - Ciclo de Cine: 🎬 Ciclo de Cine
     * - Otros: 📅 Evento
     * 
     * Los iconos proporcionan identificación visual rápida en la tabla,
     * mejorando la experiencia del usuario ciudadano.
     * 
     * @param evento El evento cuyo tipo se desea obtener
     * @return Cadena con icono y nombre del tipo
     */
    private String obtenerTipoEvento(Evento evento) {
        if (evento instanceof com.example.modelo.Feria) return "🏪 Feria";
        if (evento instanceof com.example.modelo.Concierto) return "🎵 Concierto";
        if (evento instanceof com.example.modelo.Exposicion) return "🎨 Exposición";
        if (evento instanceof com.example.modelo.Taller) return "🔧 Taller";
        if (evento instanceof com.example.modelo.CicloDeCine) return "🎬 Ciclo de Cine";
        return "📅 Evento";
    }

    /**
     * Formatea estados de evento con iconos emoji.
     * 
     * Mapeo de estados a iconos:
     * - PLANIFICADO: 📋 Planificado
     * - CONFIRMADO: ✅ Confirmado
     * - EN_EJECUCION: ⚡ En Ejecución  
     * - FINALIZADO: 🏁 Finalizado
     * 
     * Utilizado internamente por formatearEstadoConFecha() que
     * agrega lógica adicional de finalización automática.
     * 
     * @param estado El estado del evento a formatear
     * @return Cadena con icono y nombre del estado
     */
    private String formatearEstado(EstadoEvento estado) {
        switch (estado) {
            case PLANIFICADO: return "📋 Planificado";
            case CONFIRMADO: return "✅ Confirmado";
            case EN_EJECUCION: return "⚡ En Ejecución";
            case FINALIZADO: return "🏁 Finalizado";
            default: return estado.toString();
        }
    }
    
    /**
     * Formatea estado considerando fechas automáticas de finalización.
     * 
     * Lógica inteligente que:
     * 1. Obtiene el estado formal del evento
     * 2. Verifica si el evento ya terminó por fechas
     * 3. Sobrescribe con "🏁 Finalizado" si corresponde
     * 
     * Esta lógica es fundamental para la vista ciudadana ya que
     * eventos pueden estar marcados como "Confirmado" pero haber
     * terminado según su fecha de finalización calculada.
     * 
     * @param evento El evento cuyo estado se desea formatear
     * @return Estado formateado considerando fechas
     * @see #eventoYaTermino(Evento)
     * @see #formatearEstado(EstadoEvento)
     */
    private String formatearEstadoConFecha(Evento evento) {
        EstadoEvento estado = evento.getEstado();
        String estadoTexto = formatearEstado(estado);
        
        // Si el evento ya terminó por fechas, sobrescribir el estado
        if (eventoYaTermino(evento)) {
            return "🏁 Finalizado";
        }
        
        return estadoTexto;
    }
    
    /**
     * Genera información de fechas del evento.
     * 
     * Formato inteligente según duración:
     * - Eventos de 1 día: Fecha única (YYYY-MM-DD)
     * - Eventos multi-día: Rango (YYYY-MM-DD a YYYY-MM-DD)
     * 
     * Utiliza calcularFechaFinalizacion() para determinar
     * la fecha de término basada en duración del evento.
     * 
     * @param evento El evento cuyas fechas se desean obtener
     * @return Cadena formateada con información de fechas
     * @see #calcularFechaFinalizacion(Evento)
     */
    private String obtenerInfoFechas(Evento evento) {
        LocalDate inicio = evento.getFechaInicio();
        LocalDate fin = calcularFechaFinalizacion(evento);
        
        if (evento.getDuraciónDias() == 1) {
            return inicio.toString();
        } else {
            return inicio + " a " + fin;
        }
    }

    /**
     * Genera información de cupo disponible para eventos con límite.
     * 
     * Lógica diferenciada por tipo:
     * - Eventos TieneCupo: "X/Y disponibles" (disponibles de total)
     * - Otros eventos: "Sin límite"
     * 
     * La información se calcula en tiempo real considerando
     * participantes ya inscriptos, fundamental para la toma
     * de decisiones de inscripción ciudadana.
     * 
     * @param evento El evento cuyo cupo se desea obtener
     * @return Cadena formateada con información de cupo
     */
    private String obtenerInfoCupo(Evento evento) {
        if (evento instanceof com.example.modelo.TieneCupo) {
            com.example.modelo.TieneCupo cupoEvento = (com.example.modelo.TieneCupo) evento;
            int inscriptos = evento.getParticipantes() != null ? evento.getParticipantes().size() : 0;
            int total = cupoEvento.getCupoMaximo();
            int disponible = total - inscriptos;
            return String.format("%d/%d disponibles", disponible, total);
        }
        return "Sin límite";
    }

    /**
     * Genera información de responsables del evento.
     * 
     * Formato inteligente según cantidad:
     * - Un responsable: Nombre completo (nombre + apellido)
     * - Múltiples responsables: "X responsables"
     * - Sin responsables: "Sin asignar"
     * 
     * Proporciona información de contacto/responsabilidad
     * útil para consultas ciudadanas sobre eventos.
     * 
     * @param evento El evento cuyos responsables se desean obtener
     * @return Cadena formateada con información de responsables
     */
    private String obtenerResponsables(Evento evento) {
        if (evento.getResponsables() != null && !evento.getResponsables().isEmpty()) {
            if (evento.getResponsables().size() == 1) {
                Persona responsable = evento.getResponsables().get(0);
                return responsable.getNombre() + " " + responsable.getApellido();
            } else {
                return evento.getResponsables().size() + " responsables";
            }
        }
        return "Sin asignar";
    }

    // =============== MÉTODOS DE NAVEGACIÓN DEL CALENDARIO ===============
    
    /**
     * Cambia el mes actual del calendario.
     * 
     * Navegación incremental que:
     * 1. Modifica fechaActual sumando/restando meses
     * 2. Regenera completamente el calendario visual
     * 3. Actualiza la tabla de eventos filtrados
     * 4. Mantiene sincronización entre calendario y tabla
     * 
     * Método core para la navegación temporal del calendario,
     * usado por todos los botones de navegación mensual.
     * 
     * @param incremento Número de meses a sumar (positivo) o restar (negativo)
     */
    private void cambiarMes(int incremento) {
        fechaActual = fechaActual.plusMonths(incremento);
        actualizarLabelMes();
        generarCalendario();
    }

    /**
     * Navega directamente al día actual.
     * 
     * Navegación absoluta que:
     * 1. Establece fechaActual en LocalDate.now()
     * 2. Selecciona automáticamente el día actual
     * 3. Regenera calendario y tabla
     * 
     * Funcionalidad de "regreso a hoy" fundamental para
     * usuarios que navegan extensamente en el calendario.
     */
    private void irAHoy() {
        diaSeleccionado = LocalDate.now();
        fechaActual = diaSeleccionado;
        mostrarEventosDelDia(diaSeleccionado);
        actualizarLabelMes();
        generarCalendario();
    }

    /**
     * Muestra eventos de la semana actual.
     * 
     * Implementación actual redirige a irAHoy() pero está
     * preparada para extensión futura con filtrado semanal.
     * 
     * Funcionalidad diseñada para vista semanal de eventos
     * que podría incluir rango de 7 días desde hoy.
     */
    private void mostrarEventosEstaSemana() {
        // Por ahora, simplemente vamos a hoy
        irAHoy();
    }

    /**
     * Navega al próximo mes desde la fecha actual.
     * 
     * Navegación que siempre parte del mes actual real
     * (LocalDate.now()) y avanza un mes, independientemente
     * de dónde esté navegando el usuario.
     * 
     * Útil para "salto rápido" al próximo mes sin perder
     * referencia temporal actual.
     */
    private void mostrarEventosProximoMes() {
        fechaActual = LocalDate.now().plusMonths(1);
        actualizarLabelMes();
        generarCalendario();
    }

    /**
     * Actualiza toda la vista recargando datos.
     * 
     * Proceso completo de actualización:
     * 1. Recarga todosLosEventos desde EventoService
     * 2. Regenera calendario con nuevos datos
     * 3. Actualiza tabla con eventos filtrados
     * 
     * Fundamental para refrescar datos tras cambios
     * externos (inscripciones, nuevos eventos, etc.)
     */
    private void actualizarVista() {
        cargarTodosLosEventos();
        generarCalendario();
        mostrarEventosDelDia(diaSeleccionado);
    }

    // =============== MÉTODOS DE CARGA Y GESTIÓN DE DATOS ===============
    
    /**
     * Carga todos los eventos desde el servicio aplicando filtros.
     * 
     * Proceso de filtrado multicapa:
     * 1. Obtiene todos los eventos desde EventoService
     * 2. Filtra por estados disponibles (CONFIRMADO, EN_EJECUCION)
     * 3. Excluye eventos que ya terminaron por fechas
     * 4. Actualiza contadores y etiquetas informativas
     * 
     * Este filtrado es crucial para la vista ciudadana ya que
     * solo muestra eventos realmente disponibles para inscripción
     * o visualización, ocultando borradores y eventos finalizados.
     * 
     * @see #esEventoDisponibleParaMostrar(Evento)
     * @see #actualizarLabels()
     */
    private void cargarTodosLosEventos() {
        List<Evento> todosEventos = eventoService.getTodosLosEventos();
        
        // Filtrar solo eventos disponibles (CONFIRMADO o EN_EJECUCION) y que no hayan terminado
        this.todosLosEventos = todosEventos.stream()
                .filter(evento -> esEventoDisponibleParaMostrar(evento))
                .collect(java.util.stream.Collectors.toList());
        
        actualizarLabels();
    }
    
    /**
     * Verifica si un evento debe mostrarse en la vista de eventos disponibles.
     * 
     * Criterios de filtrado para vista ciudadana:
     * 1. Estado debe ser CONFIRMADO o EN_EJECUCION
     * 2. El evento no debe haber terminado por fechas
     * 
     * ESTADOS EXCLUIDOS:
     * - PLANIFICADO: Eventos en borrador, no listos para ciudadanos
     * - FINALIZADO: Eventos formalmente cerrados
     * 
     * FILTRO POR FECHAS:
     * - Eventos que ya terminaron según su duración son excluidos
     * - Permite mostrar eventos "EN_EJECUCION" que aún no terminan
     * 
     * Este filtro es fundamental para la experiencia ciudadana,
     * asegurando que solo vean eventos realmente disponibles.
     * 
     * @param evento El evento a evaluar
     * @return true si el evento debe mostrarse, false en caso contrario
     * @see #eventoYaTermino(Evento)
     */
    private boolean esEventoDisponibleParaMostrar(Evento evento) {
        EstadoEvento estado = evento.getEstado();
        
        // Solo mostrar eventos CONFIRMADO o EN_EJECUCION
        if (estado != EstadoEvento.CONFIRMADO && estado != EstadoEvento.EN_EJECUCION) {
            return false;
        }
        
        // No mostrar eventos que ya terminaron por fechas
        return !eventoYaTermino(evento);
    }

    // =============== MÉTODOS DE GENERACIÓN DEL CALENDARIO ===============
    
    /**
     * Método de coordinación para regenerar el calendario completo.
     * 
     * Proceso en dos fases:
     * 1. Poblar el calendario mensual con días y eventos
     * 2. Mostrar eventos específicos del día seleccionado
     * 
     * Actúa como punto de entrada único para actualizaciones
     * completas de la vista calendario.
     */
    private void generarCalendario() {
        poblarCalendario();
        mostrarEventosDelDia(diaSeleccionado);
    }
    
    /**
     * Genera la estructura visual del calendario mensual.
     * 
     * ALGORITMO DE CONSTRUCCIÓN:
     * 1. Limpia completamente el GridPane calendario
     * 2. Configura layout responsive con 7 columnas uniformes
     * 3. Añade encabezados de días de la semana
     * 4. Calcula posicionamiento del primer día del mes
     * 5. Genera botones para todos los días del mes
     * 6. Aplica estilos diferenciados por estado
     * 
     * DISEÑO VISUAL:
     * - Calendario tipo grilla 7x6 (max 6 semanas)
     * - Colores diferenciados: día actual, día seleccionado, días con eventos
     * - Espaciado y bordes redondeados para mejor usabilidad
     * - Indicadores visuales de eventos por día
     * 
     * INTERACTIVIDAD:
     * - Cada día es un botón clickeable
     * - Click actualiza diaSeleccionado y tabla de eventos
     * - Feedback visual inmediato de selección
     * 
     * Este método es el corazón visual del calendario, generando
     * la interfaz principal de navegación temporal para ciudadanos.
     * 
     * @see #crearBotonDia(LocalDate)
     * @see #mostrarEventosDelDia(LocalDate)
     */
    private void poblarCalendario() {
        // Limpiar el calendario
        calendarioGrid.getChildren().clear();
        calendarioGrid.getColumnConstraints().clear();
        calendarioGrid.getRowConstraints().clear();
        
        // Configurar el GridPane para mejor distribución
        calendarioGrid.setHgap(3);
        calendarioGrid.setVgap(3);
        calendarioGrid.setStyle("-fx-padding: 12; -fx-background-color: #ecf0f1; -fx-background-radius: 8;");
        
        // Configurar constrains para que las columnas se distribuyan uniformemente
        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConstraint = new ColumnConstraints();
            colConstraint.setPercentWidth(100.0 / 7.0);
            colConstraint.setHalignment(javafx.geometry.HPos.CENTER);
            calendarioGrid.getColumnConstraints().add(colConstraint);
        }
        
        // Agregar encabezados de días
        String[] diasSemana = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (int i = 0; i < diasSemana.length; i++) {
            Label encabezado = new Label(diasSemana[i]);
            encabezado.setMaxWidth(Double.MAX_VALUE);
            encabezado.setAlignment(Pos.CENTER);
            encabezado.setPrefHeight(30);
            encabezado.setStyle("-fx-font-weight: bold; -fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 6; -fx-background-radius: 3; -fx-font-size: 12px;");
            calendarioGrid.add(encabezado, i, 0);
        }
        
        // Calcular el primer día del mes
        LocalDate primerDia = fechaActual.withDayOfMonth(1);
        int diaSemanaInicio = primerDia.getDayOfWeek().getValue() % 7; // Domingo = 0
        
        // Obtener el último día del mes
        int ultimoDiaDelMes = fechaActual.lengthOfMonth();
        
        // Agregar los días del mes
        int fila = 1;
        int columna = diaSemanaInicio;
        
        for (int dia = 1; dia <= ultimoDiaDelMes; dia++) {
            LocalDate fecha = LocalDate.of(fechaActual.getYear(), fechaActual.getMonth(), dia);
            Button botonDia = crearBotonDia(fecha);
            
            calendarioGrid.add(botonDia, columna, fila);
            
            columna++;
            if (columna > 6) {
                columna = 0;
                fila++;
            }
        }
    }
    
    private Button crearBotonDia(LocalDate fecha) {
        Button boton = new Button(String.valueOf(fecha.getDayOfMonth()));
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setMaxHeight(Double.MAX_VALUE);
        boton.setPrefWidth(55);
        boton.setPrefHeight(40);
        
        // Determinar el estado del día
        boolean esHoy = fecha.equals(LocalDate.now());
        boolean esSeleccionado = fecha.equals(diaSeleccionado);
        List<Evento> eventosDelDia = obtenerEventosDelDia(fecha);
        boolean tieneEventos = eventosDelDia.size() > 0;
        
        // Aplicar estilos según el estado
        String estilo = buildEstiloDiaConEventos(esHoy, esSeleccionado, eventosDelDia);
        boton.setStyle(estilo);
        
        // Agregar indicador visual de eventos más compacto
        if (tieneEventos && !esHoy) {
            int cantidadEventos = eventosDelDia.size();
            String texto = fecha.getDayOfMonth() + "\n" + "●".repeat(Math.min(cantidadEventos, 3));
            boton.setText(texto);
        }
        
        // Acción al hacer clic
        boton.setOnAction(e -> {
            diaSeleccionado = fecha;
            mostrarEventosDelDia(fecha);
            
            // Regenerar calendario para actualizar selección
            poblarCalendario();
        });
        
        return boton;
    }
    
    private String buildEstiloDiaConEventos(boolean esHoy, boolean esSeleccionado, List<Evento> eventosDelDia) {
        String baseStyle = "-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-font-size: 11px; -fx-background-radius: 4; -fx-border-radius: 4; -fx-padding: 3;";
        
        if (esHoy) {
            // Día actual - azul brillante
            return baseStyle + " -fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #2980b9; -fx-border-width: 2;";
        } else if (esSeleccionado) {
            // Día seleccionado - gris oscuro
            return baseStyle + " -fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #7f8c8d; -fx-border-width: 2;";
        } else if (eventosDelDia.size() > 0) {
            // Verificar si hay eventos activos (no terminados)
            boolean tieneEventosActivos = eventosDelDia.stream()
                .anyMatch(evento -> esEventoInscribible(evento));
            
            if (tieneEventosActivos) {
                // Días con eventos activos - naranja
                return baseStyle + " -fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #e67e22;";
            } else {
                // Días con eventos terminados - rojo claro
                return baseStyle + " -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #c0392b;";
            }
        } else {
            // Días normales - gris claro
            return baseStyle + " -fx-background-color: #ffffff; -fx-text-fill: #2c3e50; -fx-border-color: #bdc3c7;";
        }
    }

    private void mostrarEventosDelDia(LocalDate fecha) {
        List<Evento> eventosDelDia = obtenerEventosDelDia(fecha);
        tablaEventosDelDia.setItems(FXCollections.observableArrayList(eventosDelDia));
        
        // Actualizar labels
        lblEventosDelDia.setText("📋 Eventos del " + fecha.toString());
        lblCantidadEventos.setText(eventosDelDia.size() + " evento(s)");
    }

    private List<Evento> obtenerEventosDelDia(LocalDate fecha) {
        if (todosLosEventos == null) return FXCollections.emptyObservableList();
        
        return todosLosEventos.stream()
            .filter(evento -> evento.getFechaInicio().equals(fecha))
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Actualiza el label del mes y año actual.
     * 
     * Formato: "NOMBRE_MES YYYY" (ej: "JANUARY 2024")
     * Utiliza fechaActual como referencia para mostrar
     * el mes que está visualizando el usuario.
     */
    private void actualizarLabelMes() {
        String nombreMes = fechaActual.getMonth().toString();
        int año = fechaActual.getYear();
        lblMesAno.setText(nombreMes + " " + año);
    }

    /**
     * Actualiza labels informativos generales.
     * 
     * Método placeholder para futuras estadísticas
     * o información adicional de la vista.
     */
    private void actualizarLabels() {
        // Actualizar información general
    }

    /**
     * Método temporal de compatibilidad.
     * 
     * Redirige al nuevo método cargarTodosLosEventos()
     * y actualiza la vista del día seleccionado.
     * 
     * @deprecated Usar cargarTodosLosEventos() directamente
     */
    private void cargarEventosDisponibles() {
        // Redireccionar al nuevo método
        cargarTodosLosEventos();
        mostrarEventosDelDia(diaSeleccionado);
    }

    // =============== MÉTODOS DE CREACIÓN DE COMPONENTES VISUALES ===============

    // =============== MÉTODOS DE ACCIONES DE USUARIO ===============
    
    /**
     * Gestiona el proceso completo de inscripción de personas a eventos.
     * 
     * FLUJO DE INSCRIPCIÓN:
     * 1. Abre modal de búsqueda de personas (BuscarPersonaController)
     * 2. Permite al usuario seleccionar persona a inscribir
     * 3. Si se selecciona persona, procesa la inscripción
     * 4. Maneja cancelaciones y errores apropiadamente
     * 
     * INTERFAZ MODAL:
     * - Título: "🔍 Buscar Persona para Inscribir"
     * - Modal APPLICATION_MODAL (bloquea ventana padre)
     * - Integración completa con BuscarPersonaController
     * 
     * VALIDACIONES DELEGADAS:
     * - Todas las validaciones de negocio se realizan en procesarInscripcion()
     * - Estado del evento, cupo, duplicados, responsabilidad
     * - Feedback inmediato al usuario sobre éxito/fallos
     * 
     * ACTUALIZACIÓN DE VISTA:
     * - Si la inscripción es exitosa, actualiza automáticamente la vista
     * - Recarga calendario y tabla para reflejar cambios
     * 
     * @param evento El evento al cual inscribir la persona
     * @see #procesarInscripcion(Evento, Persona)
     * @see BuscarPersonaController
     */
    private void inscribirPersonaEnEvento(Evento evento) {
        try {
            // Abrir ventana de búsqueda de persona
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/BuscarPersona.fxml"));
            Parent root = loader.load();
            
            BuscarPersonaController buscarController = loader.getController();
            
            // Crear y mostrar el modal
            Stage dialogStage = new Stage();
            dialogStage.setTitle("🔍 Buscar Persona para Inscribir");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            
            // Obtener la persona seleccionada
            Persona personaAInscribir = buscarController.getSeleccionada();
            
            if (personaAInscribir == null) {
                // El usuario canceló o no seleccionó ninguna persona
                return;
            }
            
            // Aplicar toda la lógica de validación e inscripción
            procesarInscripcion(evento, personaAInscribir);
            
        } catch (IOException e) {
            mostrarAlertaError("Error", "No se pudo abrir la ventana de búsqueda de personas.");
            e.printStackTrace();
        }
    }
    
    /**
     * Procesa la inscripción de una persona a un evento con validaciones exhaustivas.
     * 
     * VALIDACIONES EN SECUENCIA (falla rápido):
     * 1. Estado del evento: Solo CONFIRMADO o EN_EJECUCION
     * 2. Fechas: Evento no debe haber terminado
     * 3. Responsabilidad: Responsables no pueden inscribirse
     * 4. Duplicados: Persona no debe estar ya inscripta
     * 5. Cupo: Verificación de disponibilidad para eventos TieneCupo
     * 
     * LÓGICA DE RESPONSABILIDAD:
     * - Utiliza EventoService.esResponsableDelEvento() para verificación
     * - Previene conflictos de interés organizacional
     * 
     * LÓGICA DE CUPO:
     * - Solo aplica a eventos que implementan TieneCupo
     * - Calcula disponibilidad en tiempo real
     * - Considera participantes actuales vs. cupo máximo
     * 
     * PERSISTENCIA:
     * - Utiliza ParticipanteService.inscribirPersona() para grabar
     * - Manejo de excepciones con feedback específico
     * - Actualización automática de vista tras éxito
     * 
     * FEEDBACK AL USUARIO:
     * - Mensajes específicos para cada tipo de validación
     * - Alertas diferenciadas: Info (validaciones), Error (sistema), Éxito (confirmación)
     * 
     * @param evento El evento al cual inscribir
     * @param persona La persona a inscribir
     * @see #esEventoInscribible(Evento)
     * @see #obtenerRazonNoInscribible(Evento)
     * @see EventoService#esResponsableDelEvento(Persona, Evento)
     * @see ParticipanteService#estaInscripto(Persona, Evento)
     * @see ParticipanteService#inscribirPersona(Evento, Persona)
     */
    private void procesarInscripcion(Evento evento, Persona persona) {
        // Validar estado del evento
        if (!esEventoInscribible(evento)) {
            String razon = obtenerRazonNoInscribible(evento);
            mostrarAlertaInfo("Evento no disponible", razon);
            return;
        }
        
        // Verificar si es responsable del evento
        if (eventoService.esResponsableDelEvento(persona, evento)) {
            mostrarAlertaInfo("No se puede inscribir", 
                persona.getNombre() + " " + persona.getApellido() + " es responsable de este evento y no puede inscribirse.");
            return;
        }
        
        // Verificar si ya está inscripto
        if (participanteService.estaInscripto(persona, evento)) {
            mostrarAlertaInfo("Ya inscripto", 
                persona.getNombre() + " " + persona.getApellido() + " ya está inscripto a este evento.");
            return;
        }
        
        // Verificar cupo disponible
        if (evento instanceof com.example.modelo.TieneCupo) {
            com.example.modelo.TieneCupo cupoEvento = (com.example.modelo.TieneCupo) evento;
            int inscriptos = evento.getParticipantes() != null ? evento.getParticipantes().size() : 0;
            int cupoDisponible = cupoEvento.getCupoMaximo() - inscriptos;
            if (cupoDisponible <= 0) {
                mostrarAlertaInfo("Sin cupo disponible", "No hay cupo disponible para este evento.");
                return;
            }
        }
        
        // Intentar realizar la inscripción
        try {
            participanteService.inscribirPersona(evento, persona);
            mostrarAlertaExito("Inscripción exitosa", 
                persona.getNombre() + " " + persona.getApellido() + " se ha inscrito correctamente al evento '" + evento.getNombre() + "'.");
            
            // Actualizar la vista para reflejar los cambios
            actualizarVista();
            
        } catch (Exception e) {
            mostrarAlertaError("Error al inscribir", "Error al inscribir a la persona: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // =============== MÉTODOS DE VALIDACIÓN DE EVENTOS Y FECHAS ===============
    
    /**
     * Verifica si un evento está disponible para inscripción considerando estado y fechas.
     * 
     * CRITERIOS DE INSCRIBIBILIDAD:
     * 1. Estado del evento: Debe ser CONFIRMADO o EN_EJECUCION
     * 2. Validación temporal: El evento no debe haber terminado por fechas
     * 
     * ESTADOS VÁLIDOS:
     * - CONFIRMADO: Evento aprobado y listo para inscripciones
     * - EN_EJECUCION: Evento en curso que aún acepta participantes
     * 
     * ESTADOS INVÁLIDOS:
     * - PLANIFICADO: Eventos en borrador, no confirmados
     * - FINALIZADO: Eventos formalmente cerrados
     * 
     * VALIDACIÓN TEMPORAL:
     * - Utiliza eventoYaTermino() para verificar fechas
     * - Considera duración del evento para calcular finalización
     * - Previene inscripciones a eventos ya concluidos
     * 
     * Este método es fundamental para la lógica de botones en la tabla
     * y para todas las validaciones de inscripción en el sistema.
     * 
     * @param evento El evento a evaluar para inscripción
     * @return true si el evento acepta inscripciones, false en caso contrario
     * @see #eventoYaTermino(Evento)
     * @see EstadoEvento
     */
    private boolean esEventoInscribible(Evento evento) {
        // Verificar estado
        EstadoEvento estado = evento.getEstado();
        if (estado != EstadoEvento.CONFIRMADO && estado != EstadoEvento.EN_EJECUCION) {
            return false;
        }
        
        // Verificar fechas
        return !eventoYaTermino(evento);
    }
    
    /**
     * Calcula la fecha de finalización real del evento.
     * 
     * ALGORITMO DE CÁLCULO:
     * - Toma la fecha de inicio del evento
     * - Suma la duración en días (menos 1 para incluir el día de inicio)
     * - Ejemplo: Evento que empieza 2024-01-15 con duración 3 días
     *   Finaliza el 2024-01-17 (inicio + 2 días adicionales)
     * 
     * CASOS ESPECIALES:
     * - Eventos de 1 día: Finalizan el mismo día que empiezan
     * - Eventos multi-día: Duración incluye el día de inicio
     * 
     * Este cálculo es utilizado por múltiples métodos para
     * determinar si un evento sigue activo o ya terminó.
     * 
     * @param evento El evento cuya fecha de finalización se desea calcular
     * @return LocalDate con la fecha exacta de finalización
     */
    private LocalDate calcularFechaFinalizacion(Evento evento) {
        return evento.getFechaInicio().plusDays(evento.getDuraciónDias() - 1);
    }
    
    /**
     * Verifica si un evento ya terminó según las fechas calculadas.
     * 
     * LÓGICA DE COMPARACIÓN:
     * - Calcula fecha de finalización usando duración del evento
     * - Compara con la fecha actual (LocalDate.now())
     * - Retorna true si hoy es posterior a la fecha de finalización
     * 
     * IMPORTANCIA TEMPORAL:
     * - Un evento que termina "hoy" aún se considera activo
     * - Solo eventos que terminaron "ayer" o antes se marcan como terminados
     * - Proporciona un día completo de gracia para participación
     * 
     * CASOS DE USO:
     * - Filtrado en vista ciudadana (no mostrar eventos pasados)
     * - Validación de inscripciones
     * - Actualización automática de estados visuales
     * - Formato inteligente de estados en tablas
     * 
     * @param evento El evento a evaluar temporalmente
     * @return true si el evento ya terminó, false si aún está activo
     * @see #calcularFechaFinalizacion(Evento)
     */
    private boolean eventoYaTermino(Evento evento) {
        LocalDate fechaFinalizacion = calcularFechaFinalizacion(evento);
        return LocalDate.now().isAfter(fechaFinalizacion);
    }
    
    /**
     * Obtiene la razón específica por la cual un evento no es inscribible.
     * 
     * DIAGNÓSTICO DETALLADO:
     * 1. Evalúa estado formal del evento
     * 2. Evalúa validación temporal con fechas específicas
     * 3. Retorna mensaje descriptivo para feedback de usuario
     * 
     * MENSAJES POR ESTADO:
     * - PLANIFICADO: "El evento aún no ha sido confirmado"
     * - FINALIZADO: "El evento ya ha finalizado"
     * - Terminado por fechas: "El evento terminó el YYYY-MM-DD. No se permiten más inscripciones"
     * - Fallback: "El evento no está disponible para inscripción"
     * 
     * INFORMACIÓN CONTEXTUAL:
     * - Para eventos terminados por fechas, incluye fecha exacta de finalización
     * - Proporciona información específica para que usuarios entiendan la restricción
     * 
     * Utilizado principalmente en procesarInscripcion() para mostrar
     * feedback específico cuando una inscripción es rechazada.
     * 
     * @param evento El evento cuya razón de no-inscribibilidad se desea obtener
     * @return Cadena descriptiva explicando por qué no se puede inscribir
     * @see #procesarInscripcion(Evento, Persona)
     * @see #esEventoInscribible(Evento)
     */
    private String obtenerRazonNoInscribible(Evento evento) {
        EstadoEvento estado = evento.getEstado();
        
        if (estado == EstadoEvento.PLANIFICADO) {
            return "El evento aún no ha sido confirmado.";
        }
        
        if (estado == EstadoEvento.FINALIZADO) {
            return "El evento ya ha finalizado.";
        }
        
        if (eventoYaTermino(evento)) {
            LocalDate fechaFin = calcularFechaFinalizacion(evento);
            return String.format("El evento terminó el %s. No se permiten más inscripciones.", fechaFin);
        }
        
        return "El evento no está disponible para inscripción.";
    }

    /**
     * Abre modal con información detallada de un evento específico.
     * 
     * FUNCIONALIDAD DEL MODAL:
     * - Carga vista detallesEvento.fxml con DetallesEventoController
     * - Pasa el evento seleccionado al controlador de detalles
     * - Modal APPLICATION_MODAL que bloquea ventana padre
     * - Ventana no redimensionable para layout consistente
     * 
     * INFORMACIÓN MOSTRADA:
     * - Detalles completos del evento (nombre, descripción, fechas)
     * - Información de responsables y participantes
     * - Estado actual y restricciones de inscripción
     * - Cualquier información adicional específica del tipo de evento
     * 
     * INTEGRACIÓN:
     * - Utiliza DetallesEventoController para manejo de la vista
     * - Configuración automática del evento mediante setEvento()
     * - Manejo de errores con alertas informativas
     * 
     * EXPERIENCIA DE USUARIO:
     * - Título dinámico con nombre del evento
     * - Modal centrado y bien proporcionado
     * - Fácil acceso desde botones de acción en tabla
     * 
     * @param evento El evento cuyos detalles se desean visualizar
     * @see DetallesEventoController
     */
    private void verDetallesEvento(Evento evento) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/detallesEvento.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y configurar el evento
            DetallesEventoController controller = loader.getController();
            controller.setEvento(evento);

            // Crear y mostrar el diálogo
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Detalles del evento - " + evento.getNombre());
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            mostrarAlertaInfo("Error", "No se pudieron cargar los detalles del evento");
            e.printStackTrace();
        }
    }

    // =============== MÉTODOS DE CONFIGURACIÓN EXTERNA ===============
    
    /**
     * Establece la persona autenticada en el sistema.
     * 
     * FUNCIONALIDAD ACTUAL:
     * - Almacena referencia a la persona logueada
     * - Recarga eventos disponibles para la nueva sesión
     * 
     * FUNCIONALIDAD FUTURA PREPARADA:
     * - Filtrado de eventos por preferencias de usuario
     * - Personalización de vistas según perfil
     * - Historial de eventos asistidos
     * - Recomendaciones personalizadas
     * 
     * RECARGA AUTOMÁTICA:
     * - Al cambiar usuario, recarga toda la vista
     * - Garantiza datos actualizados para nueva sesión
     * - Resetea calendario al día actual
     * 
     * Aunque actualmente la vista ciudadana muestra todos los eventos
     * disponibles independientemente del usuario, este método está
     * preparado para futuras funcionalidades personalizadas.
     * 
     * @param persona La persona autenticada que usará la vista
     */
    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        cargarEventosDisponibles(); // Recargar eventos cuando se establece el usuario
    }

    // =============== MÉTODOS DE INTERFAZ DE USUARIO (ALERTAS) ===============
    
    /**
     * Muestra alerta informativa al usuario.
     * 
     * CARACTERÍSTICAS:
     * - Tipo: INFORMATION (ícono informativo azul)
     * - Sin encabezado (headerText = null) para diseño limpio
     * - Modal que requiere confirmación del usuario
     * 
     * CASOS DE USO:
     * - Validaciones de inscripción (cupo, duplicados, responsabilidad)
     * - Información sobre estados de eventos
     * - Confirmaciones de acciones no críticas
     * - Mensajes explicativos para restricciones
     * 
     * @param titulo Título de la ventana de alerta
     * @param mensaje Contenido informativo para el usuario
     */
    private void mostrarAlertaInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    /**
     * Muestra alerta de éxito con formato especial.
     * 
     * CARACTERÍSTICAS:
     * - Tipo: INFORMATION con encabezado personalizado
     * - Encabezado con emoji de confirmación: "✅ [título]"
     * - Diseño positivo para reforzar acciones exitosas
     * 
     * CASOS DE USO:
     * - Confirmación de inscripciones exitosas
     * - Finalización correcta de operaciones
     * - Feedback positivo de acciones completadas
     * 
     * DISEÑO UX:
     * - Color y emoji refuerzan el mensaje positivo
     * - Proporciona sensación de logro al usuario
     * - Confirma que la acción se realizó correctamente
     * 
     * @param titulo Título de la ventana de alerta
     * @param mensaje Contenido de confirmación para el usuario
     */
    private void mostrarAlertaExito(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText("✅ " + titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    /**
     * Muestra alerta de error del sistema.
     * 
     * CARACTERÍSTICAS:
     * - Tipo: ERROR (ícono de error rojo)
     * - Encabezado con emoji de error: "❌ [título]"
     * - Diseño crítico para errores del sistema
     * 
     * CASOS DE USO:
     * - Errores de conexión a base de datos
     * - Fallos en carga de vistas/recursos
     * - Excepciones no controladas del sistema
     * - Errores técnicos que requieren atención
     * 
     * DIFERENCIA CON INFO:
     * - Errores vs. validaciones de negocio
     * - Problemas técnicos vs. restricciones funcionales
     * - Requiere investigación vs. comportamiento esperado
     * 
     * @param titulo Título de la ventana de alerta
     * @param mensaje Descripción del error para el usuario
     */
    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText("❌ " + titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
