package com.example.controlador;

import com.example.modelo.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DetallesEventoController implements Initializable {

    // ===== ELEMENTOS BÁSICOS =====
    @FXML
    private Label lblNombre;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblResponsables;
    @FXML
    private VBox detalleEspecificoBox;

    // ===== ELEMENTOS DE PARTICIPANTES =====
    @FXML
    private ListView<Participante> listParticipantes;
    @FXML
    private Label lblContadorParticipantes;
    @FXML
    private Label lblInfoCupo;

    // ===== BOTONES DE ACCIÓN =====
    @FXML
    private Button btnCerrar;

    // ===== VARIABLES =====
    private Evento evento;
    private Stage dialogStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración inicial si es necesaria
    }

    private void configurarListaParticipantes() {
        // Configurar cómo se muestra cada participante
        listParticipantes.setCellFactory(param -> new ListCell<Participante>() {
            @Override
            protected void updateItem(Participante participante, boolean empty) {
                super.updateItem(participante, empty);
                if (empty || participante == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Persona persona = participante.getPersona();
                    setText(String.format("👤 %s %s - DNI: %d", 
                           persona.getNombre(), 
                           persona.getApellido(), 
                           persona.getDni()));
                }
            }
        });
    }

    private void cargarDatosEvento() {
        lblNombre.setText("📋 " + evento.getNombre());
        lblFecha.setText("📅 " + evento.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblEstado.setText(formatearEstado(evento.getEstado()));
        
        // Cargar responsables
        String responsables = evento.getResponsables().stream()
                .map(p -> p.getNombre() + " " + p.getApellido())
                .collect(Collectors.joining(", "));
        lblResponsables.setText("👨‍💼 Responsables: " + (responsables.isEmpty() ? "Sin asignar" : responsables));
    }
    
    private String formatearEstado(EstadoEvento estado) {
        switch (estado) {
            case PLANIFICADO: return "📋 Planificado";
            case CONFIRMADO: return "✅ Confirmado";
            case EN_EJECUCION: return "⚡ En Ejecución";
            case FINALIZADO: return "🏁 Finalizado";
            default: return "❓ " + estado.toString();
        }
    }

    private void cargarDetallesEspecificos() {
        // Limpiar detalles específicos
        detalleEspecificoBox.getChildren().clear();

        // Mostrar campos específicos según subtipo
        if (evento instanceof Taller taller) {
            Label modalidad = new Label("🎯 Modalidad: " + (taller.getModalidad() != null ? taller.getModalidad().toString() : "N/A"));
            Label instructor = new Label("👨‍🏫 Instructor: " + (taller.getInstructor() != null ? taller.getInstructor().getNombre() + " " + taller.getInstructor().getApellido() : "N/A"));
            Label cupoMax = new Label("👥 Cupo máximo: " + taller.getCupoMaximo());

            detalleEspecificoBox.getChildren().addAll(modalidad, instructor, cupoMax);

        } else if (evento instanceof Concierto concierto) {
            Label entradaGratuita = new Label("🎫 Entrada gratuita: " + (concierto.isEntradaGratuita() ? "Sí" : "No"));
            String artistas = concierto.getArtistas().stream()
                    .map(p -> p.getNombre() + " " + p.getApellido())
                    .collect(Collectors.joining(", "));
            Label artistasLabel = new Label("🎤 Artistas: " + (artistas.isEmpty() ? "N/A" : artistas));

            detalleEspecificoBox.getChildren().addAll(entradaGratuita, artistasLabel);

        } else if (evento instanceof CicloDeCine ciclo) {
            Label charlas = new Label("💬 Charlas: " + (ciclo.isHayCharlas() ? "Sí" : "No"));
            String peliculas = ciclo.getPeliculas().stream()
                    .map(p -> p.getTitulo())
                    .collect(Collectors.joining(", "));
            Label peliculasLabel = new Label("🎬 Películas: " + (peliculas.isEmpty() ? "N/A" : peliculas));

            detalleEspecificoBox.getChildren().addAll(charlas, peliculasLabel);

        } else if (evento instanceof Exposicion exposicion) {
            Label tipoArte = new Label("🎨 Tipo de arte: " + (exposicion.getTipoArte() != null ? exposicion.getTipoArte() : "N/A"));
            Label curador = new Label("👨‍🎨 Curador: " + (exposicion.getCurador() != null ? exposicion.getCurador().getNombre() + " " + exposicion.getCurador().getApellido() : "N/A"));

            detalleEspecificoBox.getChildren().addAll(tipoArte, curador);

        } else if (evento instanceof Feria feria) {
            Label cantidadStand = new Label("🏪 Cantidad de stands: " + feria.getCantidadDeStand());
            Label aireLibre = new Label("🌤️ Al aire libre: " + (feria.isAlAirelibre() ? "Sí" : "No"));

            detalleEspecificoBox.getChildren().addAll(cantidadStand, aireLibre);
        }
    }
    
    private void cargarParticipantes() {
        try {
            // Cargar participantes del evento directamente en la lista
            ObservableList<Participante> participantes = FXCollections.observableArrayList(evento.getParticipantes());
            listParticipantes.setItems(participantes);
            // Actualizar contador
            lblContadorParticipantes.setText(participantes.size() + " participante(s)");
            // Actualizar info de cupo
            actualizarInfoCupo(participantes.size());
            
        } catch (Exception e) {
            System.err.println("Error al cargar participantes: " + e.getMessage());
            lblContadorParticipantes.setText("0 participante(s)");
            actualizarInfoCupo(0);
        }
    }
    
    private void actualizarInfoCupo(int inscriptos) {
        if (evento instanceof TieneCupo) {
            TieneCupo cupoEvento = (TieneCupo) evento;
            int total = cupoEvento.getCupoMaximo();
            int disponible = total - inscriptos;
            
            if (disponible > 0) {
                lblInfoCupo.setText(String.format("✅ %d/%d cupos disponibles", disponible, total));
                lblInfoCupo.setStyle("-fx-font-size: 12px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
            } else {
                lblInfoCupo.setText(String.format("❌ Cupo completo (%d/%d)", inscriptos, total));
                lblInfoCupo.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
            }
        } else {
            lblInfoCupo.setText("♾️ Sin límite de cupo");
            lblInfoCupo.setStyle("-fx-font-size: 12px; -fx-text-fill: #17a2b8; -fx-font-weight: bold;");
        }
    }
    
    @FXML
    private void onCerrar() {
        // Cerrar el modal
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
    
    public void setEvento(Evento evento) {
        this.evento = evento;
        
        // Inicializar elementos cuando el evento esté disponible
        Platform.runLater(() -> {
            cargarDatosEvento();
            cargarDetallesEspecificos();
            cargarParticipantes();
            configurarListaParticipantes();
        });
    }
    
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
}
