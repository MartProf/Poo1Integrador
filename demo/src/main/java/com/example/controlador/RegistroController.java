package com.example.controlador;

import com.example.App;
import com.example.modelo.Persona;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistroController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtDni;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private void handleRegistrar() {
        try {
            Persona persona = new Persona();
            persona.setNombre(txtNombre.getText());
            persona.setApellido(txtApellido.getText());
            persona.setDni(Integer.parseInt(txtDni.getText()));
            persona.setTelefono(txtTelefono.getText());
            persona.setEmail(txtEmail.getText());
            persona.setUsuario(txtUsuario.getText());
            persona.setContrasena(txtContrasena.getText());

            App.getServicio().registrarPersona(persona);

            mostrarAlerta("Registro exitoso", "Usuario registrado correctamente");

            App.setRoot("sesion"); // Vuelve a la ventana de login

        } catch (Exception e) {
            mostrarAlerta("Error", "Verifica los datos ingresados");
        }
    }

    @FXML
    private void handleVolver() {
        try {
            App.setRoot("sesion");
        } catch (Exception e) {
            e.printStackTrace();
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
