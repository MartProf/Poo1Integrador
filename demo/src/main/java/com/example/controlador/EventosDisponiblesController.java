package com.example.controlador;

import com.example.modelo.Evento;
import com.example.modelo.Persona;
import com.example.servicio.EventoService;
import com.example.servicio.ParticipanteService;
import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import java.util.List;

public class EventosDisponiblesController {

    @FXML
    private TableView<Evento> tablaEventosDisponibles;

    @FXML
    private TableColumn<Evento, String> colNombre;
    @FXML
    private TableColumn<Evento, Void> colAcciones;

    private Persona personaLogueada;

    private EventoService eventoService;

    private ParticipanteService participanteService;

    public EventosDisponiblesController() {
        EntityManager em = com.example.util.JpaUtil.getEntityManager();
        this.eventoService = new EventoService(em);
        this.participanteService = new ParticipanteService(em);
    }

    public void initialize() {
        initColumns();
        cargarEventosDisponibles();
    }

    private void initColumns() {
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));

        colAcciones.setCellFactory(getBotonInscribirseCellFactory());
    }

    private void cargarEventosDisponibles() {
        List<Evento> lista = eventoService.getEventosDisponibles();
        tablaEventosDisponibles.setItems(FXCollections.observableArrayList(lista));
    }

    private Callback<TableColumn<Evento, Void>, TableCell<Evento, Void>> getBotonInscribirseCellFactory() {
        return param -> new TableCell<>() {
            private final Button btn = new Button("Inscribirse");

            {
                btn.setOnAction(event -> {
                    Evento evento = getTableView().getItems().get(getIndex());
                    try {
                        participanteService.inscribirPersona(evento, personaLogueada);
                        System.out.println("Inscripción exitosa");
                        cargarEventosDisponibles(); // refresca por si cambia cupo
                    } catch (Exception e) {
                        System.out.println("Error al inscribirse: " + e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };
    }

    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
    }
}
