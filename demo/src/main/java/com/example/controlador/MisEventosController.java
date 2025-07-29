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

public class MisEventosController {

    @FXML
    private TableView<Evento> tablaMisEventos;

    @FXML
    private TableColumn<Evento, String> colNombre;
    @FXML
    private TableColumn<Evento, String> colFecha;
    @FXML
    private TableColumn<Evento, String> colDuracion;
    @FXML
    private TableColumn<Evento, String> colTipo;
    @FXML
    private TableColumn<Evento, String> colInfoAdicional;
    @FXML
    private TableColumn<Evento, String> colTieneInscripcion;
    @FXML
    private TableColumn<Evento, String> colCantidadInscriptos;
    @FXML
    private TableColumn<Evento, String> colEstado;
    @FXML
    private TableColumn<Evento, Void> colAcciones;
    @FXML
    private TableColumn<Evento, String> colResponsables; // Nueva columna

    // Labels de estadísticas
    @FXML
    private Label lblTotalEventos;
    @FXML
    private Label lblEventosActivos;
    @FXML
    private Label lblTotalParticipantes;
    @FXML
    private Label lblEventosProximos;

    // Búsqueda
    @FXML
    private TextField txtBusqueda;
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnLimpiarBusqueda;

    // Filtros
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private ComboBox<String> cmbEstado;
    @FXML
    private ComboBox<String> cmbResponsable;
    @FXML
    private Button btnAplicarFiltros;
    @FXML
    private Button btnLimpiarFiltros;

    // Información y acciones
    @FXML
    private Label lblResultados;
    @FXML
    private Button btnActualizar;

    private Persona personaLogueada;
    private EventoService eventoService;
    
    // Listas para filtrado
    private List<Evento> todosLosEventos;
    private ObservableList<Evento> eventosFiltrados;

    public MisEventosController() {
        this.eventoService = new EventoService();
    }

    private String determinarSiRequiereInscripcion(Evento evento) {
        // Los talleres siempre requieren inscripción (por el cupo)
        if (evento instanceof Taller) {
            return "Sí";
        }
        
        // Los otros tipos pueden o no requerir inscripción según la lógica de negocio
        // Por ahora, asumo que todos pueden tener participantes opcionales
        return evento.getParticipantes() != null && !evento.getParticipantes().isEmpty() ? "Sí" : "No";
    }

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

    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        // FASE 2: Cambiar a cargar todos los eventos para gestión municipal
        cargarTodosLosEventos();
    }

    @FXML
    public void initialize() {
        initColumns();
        initComboBoxes();
        initEventHandlers();
        // Inicializar estadísticas en 0 hasta que se carguen los datos
        actualizarEstadisticas();
    }

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

    private int obtenerCantidadInscriptos(Evento evento) {
        // Verificar si el evento tiene participantes
        if (evento.getParticipantes() != null) {
            return evento.getParticipantes().size();
        }
        return 0;
    }

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

    private void actualizarEstadisticas() {
        // Método sobrecargado para inicialización
        actualizarEstadisticas(FXCollections.emptyObservableList());
    }

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

    private void actualizarInformacionResultados(List<Evento> eventos) {
        if (eventos == null) eventos = FXCollections.emptyObservableList();
        String mensaje = String.format("Mostrando %d evento%s", 
                                      eventos.size(), 
                                      eventos.size() == 1 ? "" : "s");
        if (lblResultados != null) {
            lblResultados.setText(mensaje);
        }
    }

    // Métodos de búsqueda y filtros 
    private void aplicarBusqueda() {
        // FASE 4: Integrar búsqueda con filtros
        aplicarFiltros(); // Ahora la búsqueda es parte del sistema de filtros integral
    }

    private void limpiarBusqueda() {
        // FASE 4: Limpiar solo búsqueda, mantener filtros
        if (txtBusqueda != null) {
            txtBusqueda.clear();
        }
        
        // Reaplicar filtros sin búsqueda
        aplicarFiltros();
    }

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
/*
    private void cargarMisEventos() {
        if (personaLogueada != null) {
            List<Evento> eventos = eventoService.getEventosPorResponsable(personaLogueada);
            tablaMisEventos.setItems(FXCollections.observableArrayList(eventos));
        }
    }
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
            stage.setResizable(true); // ✅ Permitir redimensionar
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // ✅ Configurar tamaño optimal para evitar problemas con barra de tareas
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setMinWidth(700);
            stage.setMinHeight(500);
            
            // ✅ Centrar la ventana automáticamente
            stage.centerOnScreen();
            
            stage.showAndWait();

            cargarTodosLosEventos(); // Recargar tabla después de editar

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void eliminarEvento(Evento evento) {
        // ✅ Confirmación simple antes de eliminar
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