package com.example.controlador;

import com.example.modelo.Evento;
import com.example.modelo.Persona;
import com.example.servicio.EventoService;
import com.example.util.JpaUtil;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.List;

public class MisEventosController {

    @FXML
    private TableView<Evento> tablaMisEventos;

    @FXML
    private TableColumn<Evento, String> colNombre;
    @FXML
    private TableColumn<Evento, String> colFecha;
    @FXML
    private TableColumn<Evento, String> colEstado;
    @FXML
    private TableColumn<Evento, Void> colAcciones;

    private Persona personaLogueada;

    private EventoService eventoService;

    public MisEventosController() {
        EntityManager em = JpaUtil.getEntityManager();
        this.eventoService = new EventoService(em);
    }

    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        cargarMisEventos();
    }

    @FXML
    public void initialize() {
        initColumns();
    }

    private void initColumns() {
        colNombre.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colFecha.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFechaInicio().toString()));
        colEstado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEstado().toString()));

        colAcciones.setCellFactory(getBotonAccionesCellFactory());
    }

    private void cargarMisEventos() {
        if (personaLogueada != null) {
            List<Evento> eventos = eventoService.getEventosPorResponsable(personaLogueada);
            tablaMisEventos.setItems(FXCollections.observableArrayList(eventos));
        }
    }

    private Callback<TableColumn<Evento, Void>, TableCell<Evento, Void>> getBotonAccionesCellFactory() {
        return param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");

            {
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
        System.out.println("Editar evento: " + evento.getNombre());
        // Aquí podrías abrir un formulario de edición usando otro FXML
    }

    private void eliminarEvento(Evento evento) {
        System.out.println("Eliminar evento: " + evento.getNombre());
        eventoService.eliminarEvento(evento);
        cargarMisEventos(); // Recargar tabla después de eliminar
    }
}
