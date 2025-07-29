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

    @FXML
    public void initialize() {
        mostrarVistaEventosDisponibles();
    }

    // Este método usamos desde SesionController
    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        if (lblUsuario != null && persona != null) {
            lblUsuario.setText(persona.getNombre() + " " + persona.getApellido());
        } 
    }

    @FXML
    public void handleMisEventos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/misEventos.fxml"));
            Parent root = loader.load();

            MisEventosController controller = loader.getController();
            controller.setPersonaLogueada(personaLogueada);

            contentPane.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNuevoEvento() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/nuevoEvento.fxml"));
            Parent root = loader.load();

            NuevoEventoController controller = loader.getController();
            controller.setPersonaLogueada(personaLogueada);

            contentPane.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePerfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/perfil.fxml"));
            Parent root = loader.load();

            PerfilController controller = loader.getController();
            controller.setPersonaLogueada(personaLogueada); // la persona logueada
            // Refrescar las inscripciones cada vez que se accede al perfil
            //controller.refrescarInscripciones();

            contentPane.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        mostrarVistaEventosDisponibles();
    }


    public void mostrarVistaEventosDisponibles() {
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
}
