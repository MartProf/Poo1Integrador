package com.example.controlador;

import com.example.modelo.Persona;
import com.example.servicio.EventoService;
import com.example.servicio.PersonaService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador para la gestión del perfil de usuario en el sistema municipal.
 * 
 * Esta clase maneja la vista de perfil personal donde los usuarios pueden
 * visualizar y modificar su información personal, así como consultar
 * estadísticas relacionadas con su actividad en el sistema como organizador
 * de eventos municipales.
 * 
 * Proporciona funcionalidades para:
 * - Edición de datos personales (nombre, apellido, email, teléfono)
 * - Validación de formato de datos
 * - Visualización de estadísticas de eventos
 * - Actualización de información en la base de datos
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Persona
 * @see PersonaService
 * @see EventoService
 */
public class PerfilController implements Initializable {

    /**
     * Referencia a la persona actualmente logueada en el sistema.
     * 
     * Almacena los datos del usuario autenticado para permitir la
     * visualización y edición de su información personal.
     */
    private Persona personaLogueada;
    
    /**
     * Servicio para operaciones relacionadas con personas.
     * 
     * Maneja las operaciones de actualización de datos personales
     * y validaciones de negocio para la gestión de usuarios.
     */
    private PersonaService personaService = new PersonaService();

    /**
     * Campo de texto para editar el nombre de la persona.
     * 
     * Permite al usuario modificar su nombre de pila con validación
     * de campo obligatorio al momento de guardar.
     */
    @FXML private TextField nombreField;
    
    /**
     * Campo de texto para editar el apellido de la persona.
     * 
     * Permite al usuario modificar su apellido con validación
     * de campo obligatorio al momento de guardar.
     */
    @FXML private TextField apellidoField;
    
    /**
     * Campo de texto para editar la dirección de email.
     * 
     * Permite al usuario modificar su email con validación de formato
     * y obligatoriedad al momento de guardar.
     */
    @FXML private TextField emailField;
    
    /**
     * Campo de texto para visualizar el DNI de la persona.
     * 
     * Aunque es editable en la interfaz, el DNI debe mantener consistencia
     * como identificador único en el sistema.
     */
    @FXML private TextField dniField;
    
    /**
     * Campo de texto para editar el número de teléfono.
     * 
     * Permite al usuario modificar su teléfono de contacto con validación
     * de campo obligatorio al momento de guardar.
     */
    @FXML private TextField telefonoField;
    
    /**
     * Botón para ejecutar la acción de guardar cambios.
     * 
     * Activa el proceso de validación y actualización de datos
     * personales en la base de datos.
     */
    @FXML private Button guardarButton;
    
    /**
     * Etiqueta para mostrar la cantidad de eventos activos.
     * 
     * Presenta estadísticas sobre eventos en estado CONFIRMADO o
     * EN_EJECUCION organizados por el usuario.
     */
    @FXML private Label lblEventosActivos;
    
    /**
     * Etiqueta para mostrar el total de participantes.
     * 
     * Presenta la suma acumulada de participantes en todos los
     * eventos organizados por el usuario.
     */
    @FXML private Label lblTotalParticipantes;
    
    /**
     * Etiqueta para mostrar eventos del mes actual.
     * 
     * Presenta la cantidad de eventos organizados por el usuario
     * en el mes en curso.
     */
    @FXML private Label lblEventosMes;
    
    /**
     * Etiqueta para mostrar la fecha de última actualización.
     * 
     * Indica cuándo fue la última vez que el usuario modificó
     * su información personal.
     */
    @FXML private Label lblUltimaActualizacion;

    /**
     * Método de inicialización llamado automáticamente por JavaFX.
     * 
     * Se ejecuta después de que se hayan cargado e inyectado todos los
     * elementos FXML. Actualmente no implementa lógica específica pero
     * está disponible para futuras configuraciones iniciales como
     * establecer validadores o configurar comportamientos de campos.
     * 
     * @param location la ubicación utilizada para resolver rutas relativas
     *                 para el objeto raíz, o null si no se conoce
     * @param resources los recursos utilizados para localizar el objeto raíz,
     *                  o null si no se han localizado
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuraciones iniciales si es necesario
    }

    /**
     * Establece la persona logueada y carga sus datos en la vista.
     * 
     * Recibe la instancia de la persona autenticada desde el controlador
     * que navegó hacia el perfil y automáticamente carga todos sus datos
     * en los campos correspondientes para visualización y edición.
     * 
     * @param persona la persona autenticada cuyo perfil se va a mostrar
     * @see #cargarDatosPersona()
     */
    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        cargarDatosPersona();
    }

    /**
     * Carga los datos de la persona logueada en los campos de la interfaz.
     * 
     * Toma la información almacenada en la instancia de personaLogueada
     * y la presenta en los campos editables de la vista. Este método
     * se ejecuta automáticamente al establecer la persona logueada y
     * después de actualizaciones exitosas.
     */
    private void cargarDatosPersona() {
        if (personaLogueada != null) {
            nombreField.setText(personaLogueada.getNombre());
            apellidoField.setText(personaLogueada.getApellido());
            emailField.setText(personaLogueada.getEmail());
            dniField.setText(String.valueOf(personaLogueada.getDni()));
            telefonoField.setText(personaLogueada.getTelefono());
        }
    }

    /**
     * Maneja el proceso de validación y actualización de datos personales.
     * 
     * Ejecuta un flujo completo de actualización que incluye:
     * 1. Validación de campos obligatorios
     * 2. Validación de formato de email
     * 3. Validación de formato y rango de DNI
     * 4. Actualización de la entidad en memoria
     * 5. Persistencia en base de datos
     * 6. Recarga de datos actualizados
     * 7. Notificación al usuario del resultado
     * 
     * Cada validación presenta mensajes específicos al usuario en caso
     * de error, deteniendo el proceso hasta que se corrijan los datos.
     * 
     * @see PersonaService#actualizarPersona(Persona)
     * @see #mostrarAlertaInfo(String, String)
     * @see #cargarDatosPersona()
     */
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

    /**
     * Muestra una alerta informativa al usuario.
     * 
     * Método utilitario que crea y presenta un diálogo modal con información
     * para el usuario. Utilizado para notificar resultados de validaciones,
     * confirmaciones de actualización exitosa, o errores durante el proceso
     * de guardado de datos.
     * 
     * El diálogo se presenta como INFORMATION type, mostrando un ícono
     * informativo y esperando confirmación del usuario antes de continuar.
     * 
     * @param titulo el título que aparecerá en la barra del diálogo
     * @param mensaje el contenido principal del mensaje a mostrar
     */
    // Método para mostrar alertas de información
    private void mostrarAlertaInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
