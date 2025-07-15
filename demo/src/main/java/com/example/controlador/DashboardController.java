package com.example.controlador;

import com.example.App;
import com.example.modelo.Persona;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class DashboardController {

    @FXML
    private Label lblUsuario;

    @FXML
    private StackPane contentPane;

    private Persona personaLogueada;

    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        lblUsuario.setText(persona.getNombre());
        cargarVista("eventos"); // Vista por defecto
    }

    @FXML
    private void handleEventos() {
        cargarVista("eventos");
    }

    @FXML
    private void handlePerfil() {
        cargarVista("perfil");
    }

    private void cargarVista(String nombreVista) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(nombreVista + ".fxml"));
            Node vista = loader.load();

            if (nombreVista.equals("perfil")) {
                PerfilController controller = loader.getController();
                controller.setPersona(personaLogueada);
            } else if (nombreVista.equals("eventos")) {
                EventosController controller = loader.getController();
                controller.setPersona(personaLogueada);
            }

            contentPane.getChildren().setAll(vista);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
