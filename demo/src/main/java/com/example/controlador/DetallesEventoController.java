package com.example.controlador;

import com.example.modelo.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.stream.Collectors;

public class DetallesEventoController {

    @FXML
    private Label lblNombre;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblDuracion;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblResponsables;

    @FXML
    private VBox detalleEspecificoBox;

    private Stage dialogStage;

    public void setEvento(Evento evento) {
        lblNombre.setText(evento.getNombre());
        lblFecha.setText("Fecha inicio: " + evento.getFechaInicio().toString());
        lblDuracion.setText("Duración (días): " + evento.getDuraciónDias());
        lblEstado.setText("Estado: " + evento.getEstado().toString());

        String responsables = evento.getResponsables().stream()
                .map(p -> p.getNombre() + " " + p.getApellido())
                .collect(Collectors.joining(", "));
        lblResponsables.setText("Responsables: " + (responsables.isEmpty() ? "N/A" : responsables));

        // Limpiar detalles específicos
        detalleEspecificoBox.getChildren().clear();

        // Mostrar campos específicos según subtipo
        if (evento instanceof Taller taller) {
            Label modalidad = new Label("Modalidad: " + (taller.getModalidad() != null ? taller.getModalidad().toString() : "N/A"));
            Label instructor = new Label("Instructor: " + (taller.getInstructor() != null ? taller.getInstructor().getNombre() + " " + taller.getInstructor().getApellido() : "N/A"));
            Label cupoMax = new Label("Cupo máximo: " + taller.getCupoMaximo());

            detalleEspecificoBox.getChildren().addAll(modalidad, instructor, cupoMax);

        } else if (evento instanceof Concierto concierto) {
            Label entradaGratuita = new Label("Entrada gratuita: " + (concierto.isEntradaGratuita() ? "Sí" : "No"));
            String artistas = concierto.getArtistas().stream()
                    .map(p -> p.getNombre() + " " + p.getApellido())
                    .collect(Collectors.joining(", "));
            Label artistasLabel = new Label("Artistas: " + (artistas.isEmpty() ? "N/A" : artistas));

            detalleEspecificoBox.getChildren().addAll(entradaGratuita, artistasLabel);

        } else if (evento instanceof CicloDeCine ciclo) {
            Label charlas = new Label("Charlas: " + (ciclo.isHayCharlas() ? "Sí" : "No"));
            String peliculas = ciclo.getPeliculas().stream()
                    .map(p -> p.getTitulo())
                    .collect(Collectors.joining(", "));
            Label peliculasLabel = new Label("Películas: " + (peliculas.isEmpty() ? "N/A" : peliculas));

            detalleEspecificoBox.getChildren().addAll(charlas, peliculasLabel);

        } else if (evento instanceof Exposicion exposicion) {
            Label tipoArte = new Label("Tipo de arte: " + (exposicion.getTipoArte() != null ? exposicion.getTipoArte() : "N/A"));
            Label curador = new Label("Curador: " + (exposicion.getCurador() != null ? exposicion.getCurador().getNombre() + " " + exposicion.getCurador().getApellido() : "N/A"));

            detalleEspecificoBox.getChildren().addAll(tipoArte, curador);

        } else if (evento instanceof Feria feria) {
            Label cantidadStand = new Label("Cantidad de stands: " + feria.getCantidadDeStand());
            Label aireLibre = new Label("Al aire libre: " + (feria.isAlAirelibre() ? "Sí" : "No"));

            detalleEspecificoBox.getChildren().addAll(cantidadStand, aireLibre);
        }
    }

    @FXML
    private void handleCerrar() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
}
