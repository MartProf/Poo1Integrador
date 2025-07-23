package com.example.controlador;

import com.example.modelo.*;
import com.example.servicio.EventoService;
import com.example.util.JpaUtil;

import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    private Persona personaLogueada;
    private EventoService eventoService;

    public MisEventosController() {
        EntityManager em = JpaUtil.getEntityManager();
        this.eventoService = new EventoService(em);
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
        cargarMisEventos();
    }

    @FXML
    public void initialize() {
        initColumns();
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
            return String.format("Entrada: %s, Artistas: %d", 
                concierto.isEntradaGratuita() ? "Gratuita" : "Paga",
                concierto.getArtistas() != null ? concierto.getArtistas().size() : 0);
                
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
            return String.format("%s, Películas: %d", 
                ciclo.isHayCharlas() ? "Con charlas" : "Sin charlas",
                ciclo.getPeliculas() != null ? ciclo.getPeliculas().size() : 0);
                
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
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            cargarMisEventos(); // Recargar tabla después de editar

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void eliminarEvento(Evento evento) {
        // Aquí podrías agregar una confirmación antes de eliminar
        System.out.println("Eliminar evento: " + evento.getNombre());
        eventoService.eliminarEvento(evento);
        cargarMisEventos(); // Recargar tabla después de eliminar
    }

}