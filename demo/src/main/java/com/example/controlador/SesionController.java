package com.example.controlador;

import com.example.App;
import com.example.modelo.Persona;
import com.example.servicio.Servicio;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SesionController {

    @FXML
    private TextField txtUsu;

    @FXML
    private PasswordField TxtCon;

    @FXML
    private void initialize() {
        // Opcional: lógica al cargar la vista
    }

    @FXML
    private void handleIniciarSesion() {
        String usuario = txtUsu.getText();
        String contrasena = TxtCon.getText();

        Servicio servicio = App.getServicio();
        Persona persona = servicio.login(usuario, contrasena);

        if (persona != null) {
            try {
                FXMLLoader loader = App.setRoot("dashboard");
                DashboardController controller = loader.getController();
                controller.setPersonaLogueada(persona);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Error", "Usuario o contraseña incorrectos");
        }
    }


    @FXML
    private void handleRegistrarse() {
        try {
            App.setRoot("registro");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de registro.");
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
