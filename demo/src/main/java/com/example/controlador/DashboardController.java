package com.example.controlador;

import com.example.App;
import com.example.modelo.Persona;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class DashboardController {

    @FXML
    private StackPane contentPane;

    @FXML
    private Label lblUsuario;

    private Persona personaLogueada;



    // Este método usamos desde SesionController
    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        if (lblUsuario != null && persona != null) {
            lblUsuario.setText(persona.getNombre() + " " + persona.getApellido());
        }
    }

    @FXML
    public void handleMisEventos() {
        setContent("misEventos.fxml");
    }

    @FXML
    public void handleNuevoEvento() {
        setContent("nuevoEvento.fxml");
    }

    @FXML
    public void handlePerfil() {
        setContent("perfil.fxml");
    }

    @FXML
    private void handleCerrarSesion() {
        try {
            App.setRoot("sesion"); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEventosDisponibles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/eventos_disponibles.fxml"));
            Parent root = loader.load();

            EventosDisponiblesController controller = loader.getController();
            controller.setPersonaLogueada(personaLogueada);

            contentPane.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setContent(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/" + fxml));
            StackPane pane = loader.load();

            // Opcional: pasar personaLogueada a la vista cargada
            // Dashboard siempre mantiene el usuario
            contentPane.getChildren().clear();
            contentPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
