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

public class EventosDisponiblesController {

    // ===== ELEMENTOS DEL CALENDARIO =====
    @FXML
    private GridPane calendarioGrid;
    @FXML
    private Label lblMesAno;
    @FXML
    private Button btnMesAnterior;
    @FXML
    private Button btnMesSiguiente;

    // ===== TABLA DE EVENTOS DEL DÍA =====
    @FXML
    private TableView<Evento> tablaEventosDelDia;
    @FXML
    private TableColumn<Evento, String> colNombre;
    @FXML
    private TableColumn<Evento, String> colTipo;
    @FXML
    private TableColumn<Evento, String> colHorario;
    @FXML
    private TableColumn<Evento, String> colDuracion;
    @FXML
    private TableColumn<Evento, String> colEstado;
    @FXML
    private TableColumn<Evento, String> colCupo;
    @FXML
    private TableColumn<Evento, String> colResponsables;
    @FXML
    private TableColumn<Evento, Void> colAcciones;

    // ===== LABELS INFORMATIVOS =====
    @FXML
    private Label lblEventosDelDia;
    @FXML
    private Label lblCantidadEventos;

    // ===== BOTONES DE FILTROS RÁPIDOS =====
    @FXML
    private Button btnHoy;
    @FXML
    private Button btnEstaSemana;
    @FXML
    private Button btnProximoMes;
    @FXML
    private Button btnActualizar;

    // ===== DATOS Y SERVICIOS =====
    private Persona personaLogueada;
    private EventoService eventoService;
    private ParticipanteService participanteService;
    
    // ===== VARIABLES DEL CALENDARIO =====
    private java.time.LocalDate fechaActual;
    private java.time.LocalDate diaSeleccionado;
    private List<Evento> todosLosEventos;

    public EventosDisponiblesController() {
        this.eventoService = new EventoService();
        this.participanteService = new ParticipanteService();
        this.fechaActual = java.time.LocalDate.now();
        this.diaSeleccionado = java.time.LocalDate.now();
    }

    @FXML
    public void initialize() {
        initColumns();
        initEventHandlers();
        cargarTodosLosEventos();
        generarCalendario();
        mostrarEventosDelDia(diaSeleccionado);
    }

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

    // ===== MÉTODOS AUXILIARES PARA LAS COLUMNAS =====
    private String obtenerTipoEvento(Evento evento) {
        if (evento instanceof com.example.modelo.Feria) return "🏪 Feria";
        if (evento instanceof com.example.modelo.Concierto) return "🎵 Concierto";
        if (evento instanceof com.example.modelo.Exposicion) return "🎨 Exposición";
        if (evento instanceof com.example.modelo.Taller) return "🔧 Taller";
        if (evento instanceof com.example.modelo.CicloDeCine) return "🎬 Ciclo de Cine";
        return "📅 Evento";
    }

    private String formatearEstado(EstadoEvento estado) {
        switch (estado) {
            case PLANIFICADO: return "📋 Planificado";
            case CONFIRMADO: return "✅ Confirmado";
            case EN_EJECUCION: return "⚡ En Ejecución";
            case FINALIZADO: return "🏁 Finalizado";
            default: return estado.toString();
        }
    }
    
    private String formatearEstadoConFecha(Evento evento) {
        EstadoEvento estado = evento.getEstado();
        String estadoTexto = formatearEstado(estado);
        
        // Si el evento ya terminó por fechas, sobrescribir el estado
        if (eventoYaTermino(evento)) {
            return "🔴 Terminado";
        }
        
        return estadoTexto;
    }
    
    private String obtenerInfoFechas(Evento evento) {
        LocalDate inicio = evento.getFechaInicio();
        LocalDate fin = calcularFechaFinalizacion(evento);
        
        if (evento.getDuraciónDias() == 1) {
            return inicio.toString();
        } else {
            return inicio + " a " + fin;
        }
    }

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

    // ===== MÉTODOS DEL CALENDARIO =====
    private void cargarTodosLosEventos() {
        todosLosEventos = eventoService.getTodosLosEventos();
        actualizarLabels();
    }

    private void generarCalendario() {
        poblarCalendario();
        mostrarEventosDelDia(diaSeleccionado);
    }
    
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

    // ===== MÉTODOS DE NAVEGACIÓN =====
    private void cambiarMes(int incremento) {
        fechaActual = fechaActual.plusMonths(incremento);
        actualizarLabelMes();
        generarCalendario();
    }

    private void irAHoy() {
        diaSeleccionado = java.time.LocalDate.now();
        fechaActual = diaSeleccionado;
        mostrarEventosDelDia(diaSeleccionado);
        actualizarLabelMes();
        generarCalendario();
    }

    private void mostrarEventosEstaSemana() {
        // Por ahora, mostrar eventos de hoy
        irAHoy();
    }

    private void mostrarEventosProximoMes() {
        fechaActual = fechaActual.plusMonths(1);
        actualizarLabelMes();
        generarCalendario();
    }

    private void actualizarVista() {
        cargarTodosLosEventos();
        generarCalendario();
        mostrarEventosDelDia(diaSeleccionado);
    }

    private void actualizarLabelMes() {
        String nombreMes = fechaActual.getMonth().toString();
        int año = fechaActual.getYear();
        lblMesAno.setText(nombreMes + " " + año);
    }

    private void actualizarLabels() {
        // Actualizar información general
    }

    // ===== MÉTODO TEMPORAL PARA COMPATIBILIDAD =====
    private void cargarEventosDisponibles() {
        // Redireccionar al nuevo método
        cargarTodosLosEventos();
        mostrarEventosDelDia(diaSeleccionado);
    }

    // ===== MÉTODOS DE ACCIONES =====
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
    
    // ===== MÉTODOS DE VALIDACIÓN DE FECHAS =====
    
    /**
     * Verifica si un evento está disponible para inscripción considerando estado y fechas
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
     * Calcula la fecha de finalización del evento
     */
    private LocalDate calcularFechaFinalizacion(Evento evento) {
        return evento.getFechaInicio().plusDays(evento.getDuraciónDias() - 1);
    }
    
    /**
     * Verifica si un evento ya terminó según las fechas
     */
    private boolean eventoYaTermino(Evento evento) {
        LocalDate fechaFinalizacion = calcularFechaFinalizacion(evento);
        return LocalDate.now().isAfter(fechaFinalizacion);
    }
    
    /**
     * Obtiene la razón por la cual un evento no es inscribible
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

    private Callback<TableColumn<Evento, Void>, TableCell<Evento, Void>> getBotonInscribirseCellFactory() {
        return param -> new TableCell<>() {
            private final Button btnInscribir = new Button("Inscribirse");
            private final Button btnVerDetalle = new Button("Ver detalles");
            private final HBox pane = new HBox(5, btnInscribir, btnVerDetalle);

            {
                btnInscribir.setOnAction(event -> {
                    Evento evento = getTableView().getItems().get(getIndex());
                    
                    // Verificar que hay una persona logueada
                    if (personaLogueada == null) {
                        mostrarAlertaInfo("Acceso denegado", "Debes iniciar sesión para inscribirte a eventos.");
                        return;
                    }
                    
                    if (evento.getEstado() != EstadoEvento.CONFIRMADO) {
                        mostrarAlertaInfo("Evento no disponible", "El evento no está disponible para inscripción.");
                        return;
                    }
                    
                    // Verificar si es responsable del evento
                    if (eventoService.esResponsableDelEvento(personaLogueada, evento)) {
                        mostrarAlertaInfo("No puedes inscribirte", "No puedes inscribirte a un evento del cual eres responsable.");
                        return;
                    }
                    
                    // Verificar si ya está inscripto
                    if (participanteService.estaInscripto(personaLogueada, evento)) {
                        mostrarAlertaInfo("Ya inscripto", "Ya estás inscripto a este evento.");
                        return;
                    }
                    
                    if (evento instanceof com.example.modelo.TieneCupo) {
                        com.example.modelo.TieneCupo cupoEvento = (com.example.modelo.TieneCupo) evento;
                        int cupoDisponible = cupoEvento.getCupoMaximo() - evento.getParticipantes().size();
                        if (cupoDisponible <= 0) {
                            mostrarAlertaInfo("No hay cupo disponible", "No hay cupo disponible para este evento.");
                            return;
                        }
                    }
                    try {
                        participanteService.inscribirPersona(evento, personaLogueada);
                        mostrarAlertaInfo("Inscripción exitosa", "Te has inscrito correctamente al evento.");
                        cargarEventosDisponibles(); // refresca por si cambia cupo
                    } catch (Exception e) {
                        mostrarAlertaInfo("Error al inscribirse", e.getMessage());
                    }
                });

                btnVerDetalle.setOnAction(event -> {
                    Evento evento = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/DetallesEvento.fxml"));
                        Parent root = loader.load();

                        DetallesEventoController controller = loader.getController();
                        controller.setEvento(evento);

                        Stage dialogStage = new Stage();
                        dialogStage.setTitle("Detalles del evento");
                        dialogStage.setScene(new Scene(root));
                        controller.setDialogStage(dialogStage);

                        dialogStage.initModality(Modality.APPLICATION_MODAL);
                        dialogStage.showAndWait();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Evento evento = getTableView().getItems().get(getIndex());

                    // Validar si requiere inscripción: por ejemplo, estado == CONFIRMADO
                    boolean requiereInscripcion = evento.getEstado() == EstadoEvento.CONFIRMADO;

                    // Si tiene cupo (es instancia de TieneCupo), verificar cupo disponible
                    boolean hayCupo = true;
                    if (evento instanceof com.example.modelo.TieneCupo) {
                        com.example.modelo.TieneCupo cupoEvento = (com.example.modelo.TieneCupo) evento;
                        int cupoDisponible = cupoEvento.getCupoMaximo() - evento.getParticipantes().size();
                        hayCupo = cupoDisponible > 0;
                    }

                    // Verificar si ya está inscripto
                    boolean yaInscripto = personaLogueada != null && 
                                         participanteService.estaInscripto(personaLogueada, evento);

                    // Verificar si es responsable del evento
                    boolean esResponsable = personaLogueada != null && 
                                           eventoService.esResponsableDelEvento(personaLogueada, evento);

                    // Verificar si hay una persona logueada
                    boolean hayPersonaLogueada = personaLogueada != null;

                    btnInscribir.setDisable(!(hayPersonaLogueada && requiereInscripcion && hayCupo && !yaInscripto && !esResponsable));

                    setGraphic(pane);
                }
            }
        };
    }

    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        cargarEventosDisponibles(); // Recargar eventos cuando se establece el usuario
    }

    //Metodo para mostrar alertas de informacion
    private void mostrarAlertaInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    //Metodo para mostrar alertas de éxito
    private void mostrarAlertaExito(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText("✅ " + titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    //Metodo para mostrar alertas de error
    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText("❌ " + titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
