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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class EventosDisponiblesController {

    @FXML
    private TableView<Evento> tablaEventosDisponibles;
    @FXML
    private TableColumn<Evento, String> colNombre;
    @FXML
    private TableColumn<Evento, String> colFecha;
    @FXML
    private TableColumn<Evento, String> colDuracion;
    @FXML
    private TableColumn<Evento, String> colTipo;
    @FXML
    private TableColumn<Evento, String> colEstado;
    @FXML
    private TableColumn<Evento, String> colCupo;
    @FXML
    private TableColumn<Evento, String> colResponsables;
    @FXML
    private TableColumn<Evento, Void> colAcciones;

    private Persona personaLogueada;

    private EventoService eventoService;

    private ParticipanteService participanteService;

    public EventosDisponiblesController() {
        this.eventoService = new EventoService();
        this.participanteService = new ParticipanteService();
    }

    public void initialize() {
        initColumns();
        cargarEventosDisponibles();
    }

    private void initColumns() {
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));

        colFecha.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getFechaInicio().toString()));

        colDuracion.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getDuraciónDias())));

        colTipo.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getClass().getSimpleName()));

        colEstado.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado().toString()));

        colResponsables.setCellValueFactory(data -> {
            List<Persona> responsables = data.getValue().getResponsables();
            String nombres = responsables.stream()
                    .map(p -> p.getNombre() + " " + p.getApellido())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("-");
            return new javafx.beans.property.SimpleStringProperty(nombres);
        });

        colCupo.setCellValueFactory(data -> {
            Evento evento = data.getValue();
            if (evento instanceof com.example.modelo.TieneCupo) {
                com.example.modelo.TieneCupo cupoEvento = (com.example.modelo.TieneCupo) evento;
                int cupoDisponible = cupoEvento.getCupoMaximo() - evento.getParticipantes().size();
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(cupoDisponible));
            } else {
                return new javafx.beans.property.SimpleStringProperty("Sin límite");
            }
        });

        colAcciones.setCellFactory(getBotonInscribirseCellFactory());
    }

    private void cargarEventosDisponibles() {
        List<Evento> lista = eventoService.getEventosDisponibles();
        tablaEventosDisponibles.setItems(FXCollections.observableArrayList(lista));
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
}
