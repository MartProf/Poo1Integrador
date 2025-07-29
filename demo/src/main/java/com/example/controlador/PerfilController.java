package com.example.controlador;

import com.example.modelo.Persona;
import com.example.servicio.EventoService;
import com.example.servicio.PersonaService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class PerfilController implements Initializable {

    private Persona personaLogueada;
    private PersonaService personaService = new PersonaService();
    private EventoService eventoService = new EventoService();

    // Campos de datos personales
    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private TextField emailField;
    @FXML private TextField dniField;
    @FXML private TextField telefonoField;
    @FXML private Button guardarButton;
    
    // Labels de estadísticas
    @FXML private Label lblEventosActivos;
    @FXML private Label lblTotalParticipantes;
    @FXML private Label lblEventosMes;
    @FXML private Label lblUltimaActualizacion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuraciones iniciales si es necesario
    }

    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        cargarDatosPersona();
    }

    private void cargarDatosPersona() {
        if (personaLogueada != null) {
            nombreField.setText(personaLogueada.getNombre());
            apellidoField.setText(personaLogueada.getApellido());
            emailField.setText(personaLogueada.getEmail());
            dniField.setText(String.valueOf(personaLogueada.getDni()));
            telefonoField.setText(personaLogueada.getTelefono());
        }
    }

    @FXML
    private void guardarDatos() {
        if (personaLogueada != null) {
            try {
                // Validar que los campos no estén vacíos
                if (nombreField.getText() == null || nombreField.getText().trim().isEmpty()) {
                    mostrarAlertaInfo("Campo requerido", "El nombre es obligatorio.");
                    return;
                }
                
                if (apellidoField.getText() == null || apellidoField.getText().trim().isEmpty()) {
                    mostrarAlertaInfo("Campo requerido", "El apellido es obligatorio.");
                    return;
                }
                
                if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
                    mostrarAlertaInfo("Campo requerido", "El email es obligatorio.");
                    return;
                }
                
                if (dniField.getText() == null || dniField.getText().trim().isEmpty()) {
                    mostrarAlertaInfo("Campo requerido", "El DNI es obligatorio.");
                    return;
                }
                
                if (telefonoField.getText() == null || telefonoField.getText().trim().isEmpty()) {
                    mostrarAlertaInfo("Campo requerido", "El teléfono es obligatorio.");
                    return;
                }

                // Validar formato del email
                String email = emailField.getText().trim();
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    mostrarAlertaInfo("Email inválido", "Por favor ingrese un email válido.");
                    return;
                }

                // Validar que el DNI sea un número válido
                int dni;
                try {
                    dni = Integer.parseInt(dniField.getText().trim());
                    if (dni <= 0) {
                        mostrarAlertaInfo("DNI inválido", "El DNI debe ser un número positivo.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlertaInfo("DNI inválido", "El DNI debe ser un número válido.");
                    return;
                }

                // Si todas las validaciones pasan, actualizar los datos
                personaLogueada.setNombre(nombreField.getText().trim());
                personaLogueada.setApellido(apellidoField.getText().trim());
                personaLogueada.setEmail(email);
                personaLogueada.setDni(dni);
                personaLogueada.setTelefono(telefonoField.getText().trim());

                personaService.actualizarPersona(personaLogueada);


                mostrarAlertaInfo("Éxito", "Datos actualizados correctamente.");
                cargarDatosPersona(); // Recargar datos actualizados
                
            } catch (Exception e) {
                mostrarAlertaInfo("Error", "Ocurrió un error al actualizar los datos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Método para mostrar alertas de información
    private void mostrarAlertaInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
