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

/**
 * Controlador para la ventana de detalles de un evento.
 * Muestra información general, responsables, participantes y datos específicos según el tipo de evento.
 */
public class DetallesEventoController implements Initializable {

    // ======== ELEMENTOS DE LA VISTA (INYECTADOS DESDE FXML) ========
    @FXML private Label lblNombre;
    @FXML private Label lblFecha;
    @FXML private Label lblEstado;
    @FXML private Label lblResponsables;
    @FXML private VBox detalleEspecificoBox;

    @FXML private ListView<Participante> listParticipantes;
    @FXML private Label lblContadorParticipantes;
    @FXML private Label lblInfoCupo;

    @FXML private Button btnCerrar;

    // ======== VARIABLES INTERNAS ========
    private Evento evento;
    private Stage dialogStage;

    /**
     * Método que se ejecuta al inicializar el controlador.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Se puede usar para lógica de inicialización si es necesario
    }

    /**
     * Configura cómo se muestran los elementos en la lista de participantes.
     */
    private void configurarListaParticipantes() {
        listParticipantes.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Participante participante, boolean empty) {
                super.updateItem(participante, empty);
                if (empty || participante == null) {
                    setText(null);
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

    /**
     * Carga y muestra los datos básicos del evento.
     */
    private void cargarDatosEvento() {
        lblNombre.setText("📋 " + evento.getNombre());
        lblFecha.setText("📅 " + evento.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblEstado.setText(formatearEstado(evento.getEstado()));

        // Muestra los nombres de los responsables separados por coma
        String responsables = evento.getResponsables().stream()
                .map(p -> p.getNombre() + " " + p.getApellido())
                .collect(Collectors.joining(", "));
        lblResponsables.setText("👨‍💼 Responsables: " + (responsables.isEmpty() ? "Sin asignar" : responsables));
    }

    /**
     * Convierte el estado del evento en un texto más legible y decorado con emoji.
     */
    private String formatearEstado(EstadoEvento estado) {
        return switch (estado) {
            case PLANIFICADO -> "📋 Planificado";
            case CONFIRMADO -> "✅ Confirmado";
            case EN_EJECUCION -> "⚡ En Ejecución";
            case FINALIZADO -> "🏁 Finalizado";
            default -> "❓ " + estado.toString();
        };
    }

    /**
     * Muestra detalles específicos según el subtipo del evento (Taller, Concierto, Feria, etc.).
     */
    private void cargarDetallesEspecificos() {
        detalleEspecificoBox.getChildren().clear(); // Limpiar primero

        if (evento instanceof Taller taller) {
            Label modalidad = new Label("🎯 Modalidad: " + (taller.getModalidad() != null ? taller.getModalidad() : "N/A"));
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
                    .map(Pelicula::getTitulo)
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

    /**
     * Carga los participantes del evento y los muestra en la lista.
     */
    private void cargarParticipantes() {
        try {
            ObservableList<Participante> participantes = FXCollections.observableArrayList(evento.getParticipantes());
            listParticipantes.setItems(participantes);

            lblContadorParticipantes.setText(participantes.size() + " participante(s)");
            actualizarInfoCupo(participantes.size());

        } catch (Exception e) {
            System.err.println("Error al cargar participantes: " + e.getMessage());
            lblContadorParticipantes.setText("0 participante(s)");
            actualizarInfoCupo(0);
        }
    }

    /**
     * Muestra información sobre el cupo disponible, si aplica.
     */
    private void actualizarInfoCupo(int inscriptos) {
        if (evento instanceof TieneCupo cupoEvento) {
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

    /**
     * Evento asociado al botón "Cerrar". Cierra la ventana.
     */
    @FXML
    private void onCerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    /**
     * Establece el evento del cual se van a mostrar los detalles.
     * Llama a los métodos de carga para poblar la interfaz.
     */
    public void setEvento(Evento evento) {
        this.evento = evento;

        // Ejecuta la carga en el hilo de la interfaz gráfica
        Platform.runLater(() -> {
            cargarDatosEvento();
            cargarDetallesEspecificos();
            cargarParticipantes();
            configurarListaParticipantes();
        });
    }

    /**
     * Establece la ventana asociada (por ejemplo, un diálogo modal).
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
}
