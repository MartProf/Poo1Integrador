package com.example.controlador;

import com.example.App;
import com.example.modelo.Persona;
import com.example.servicio.PersonaService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private Label lblUsuario;

    @FXML
    private Label lblContrasena;

    // Variables para modo modal
    private boolean modoModal = false;
    private Persona personaRegistrada = null;

    public void initialize() {
        // Método que se ejecuta después de cargar el FXML
    }

    @FXML
    private void handleRegistrar() {
        try {
            // Validaciones básicas de UI (campos vacíos)
            if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
                mostrarAlerta("Campo requerido", "El nombre es obligatorio.");
                return;
            }
            if (txtApellido.getText() == null || txtApellido.getText().trim().isEmpty()) {
                mostrarAlerta("Campo requerido", "El apellido es obligatorio.");
                return;
            }
            if (txtDni.getText() == null || txtDni.getText().trim().isEmpty()) {
                mostrarAlerta("Campo requerido", "El DNI es obligatorio.");
                return;
            }
            if (txtTelefono.getText() == null || txtTelefono.getText().trim().isEmpty()) {
                mostrarAlerta("Campo requerido", "El teléfono es obligatorio.");
                return;
            }
            if (txtEmail.getText() == null || txtEmail.getText().trim().isEmpty()) {
                mostrarAlerta("Campo requerido", "El email es obligatorio.");
                return;
            }

            // En modo normal validar usuario y contraseña
            if (!modoModal) {
                if (txtUsuario.getText() == null || txtUsuario.getText().trim().isEmpty()) {
                    mostrarAlerta("Campo requerido", "El usuario es obligatorio.");
                    return;
                }
                if (txtContrasena.getText() == null || txtContrasena.getText().trim().isEmpty()) {
                    mostrarAlerta("Campo requerido", "La contraseña es obligatoria.");
                    return;
                }
            }

            // Validación de formato de DNI
            int dni;
            try {
                dni = Integer.parseInt(txtDni.getText().trim());
            } catch (NumberFormatException e) {
                mostrarAlerta("DNI inválido", "El DNI debe ser un número válido.");
                return;
            }

            // Crear la persona
            Persona persona = new Persona();
            persona.setNombre(txtNombre.getText().trim());
            persona.setApellido(txtApellido.getText().trim());
            persona.setDni(dni);
            persona.setTelefono(txtTelefono.getText().trim());
            persona.setEmail(txtEmail.getText().trim());

            if (modoModal) {
                // Modo modal: registro simple sin usuario/contraseña
                persona.setUsuario(null);
                persona.setContrasena(null);
                
                // Usar el servicio simple que creamos
                PersonaService personaService = new PersonaService();
                personaRegistrada = personaService.guardarPersonaSimple(persona);
                
                mostrarAlerta("Registro exitoso", "Persona registrada correctamente");
                // Cerrar modal
                Stage stage = (Stage) txtNombre.getScene().getWindow();
                stage.close();
                
            } else {
                // Modo normal: registro completo
                persona.setUsuario(txtUsuario.getText().trim());
                persona.setContrasena(txtContrasena.getText().trim());

                // El servicio se encarga de todas las validaciones de negocio
                App.getPersonaService().registrarPersona(persona);

                mostrarAlerta("Registro exitoso", "Usuario registrado correctamente");
                App.setRoot("sesion"); // Vuelve a la ventana de login
            }

        } catch (IllegalArgumentException e) {
            // Mostrar el mensaje específico del servicio
            mostrarAlerta("Error de validación", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta("Error", "Ocurrió un error inesperado al registrar el usuario.");
            e.printStackTrace();
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

    // Métodos para modo modal
    public void setModoModal(boolean modoModal) {
        this.modoModal = modoModal;
        // En modo modal, ocultar campos de usuario y contraseña
        if (modoModal) {
            lblUsuario.setVisible(false);
            txtUsuario.setVisible(false);
            lblContrasena.setVisible(false);
            txtContrasena.setVisible(false);
        }
    }

    public Persona getPersonaRegistrada() {
        return personaRegistrada;
    }
}
