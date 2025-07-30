package com.example.controlador;

import com.example.App;
import com.example.modelo.Persona;
import com.example.servicio.PersonaService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controlador para la gestión de inicio de sesión en el sistema municipal.
 * 
 * Esta clase maneja la vista de login del sistema, proporcionando funcionalidades
 * para autenticar usuarios y redirigir al dashboard principal o permitir el
 * registro de nuevos usuarios. Actúa como punto de entrada principal al sistema
 * para usuarios ya registrados.
 * 
 * La autenticación se realiza mediante usuario y contraseña, validando las
 * credenciales contra la base de datos a través del servicio correspondiente.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see DashboardController
 * @see RegistroController
 * @see Persona
 * @see PersonaService
 */
public class SesionController {

    /**
     * Campo de texto para ingresar el nombre de usuario.
     * 
     * Permite al usuario introducir sus credenciales de acceso al sistema.
     * Este campo está vinculado al archivo FXML correspondiente y es
     * obligatorio para el proceso de autenticación.
     */
    @FXML
    private TextField txtUsu;

    /**
     * Campo de contraseña para ingresar la clave de acceso.
     * 
     * Campo especializado que oculta los caracteres ingresados por seguridad.
     * Junto con el usuario, forma las credenciales necesarias para acceder
     * al sistema municipal.
     */
    @FXML
    private PasswordField TxtCon;

    /**
     * Servicio para operaciones relacionadas con personas.
     * 
     * Maneja la autenticación de usuarios y todas las operaciones
     * de negocio relacionadas con personas en el sistema.
     */
    private PersonaService personaService = new PersonaService();

    /**
     * Método de inicialización llamado automáticamente por JavaFX.
     * 
     * Se ejecuta después de que se hayan cargado e inyectado todos los
     * elementos FXML. Actualmente no implementa lógica específica pero
     * está disponible para futuras configuraciones iniciales.
     */
    @FXML
    private void initialize() {
        // Opcional: lógica al cargar la vista
    }

    /**
     * Maneja el proceso de inicio de sesión del usuario.
     * 
     * Obtiene las credenciales ingresadas, las valida contra la base de datos
     * a través del servicio de personas, y en caso de éxito redirige al
     * dashboard principal estableciendo la persona logueada. Si las credenciales
     * son incorrectas, muestra un mensaje de error al usuario.
     * 
     * El método realiza las siguientes acciones:
     * 1. Extrae usuario y contraseña de los campos de texto
     * 2. Invoca al servicio de autenticación
     * 3. Si es exitoso: carga el dashboard y establece la sesión
     * 4. Si falla: muestra alerta de credenciales incorrectas
     * 
     * @see PersonaService#login(String, String)
     * @see DashboardController#setPersonaLogueada(Persona)
     */
    @FXML
    private void handleIniciarSesion() {
        String usuario = txtUsu.getText();
        String contrasena = TxtCon.getText();

        Persona persona = personaService.login(usuario, contrasena);

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

    /**
     * Redirige a la ventana de registro de nuevos usuarios.
     * 
     * Cambia la vista actual hacia el formulario de registro, permitiendo
     * que usuarios no registrados puedan crear una cuenta nueva en el
     * sistema municipal. En caso de error al cargar la vista, muestra
     * un mensaje informativo al usuario.
     * 
     * @see App#setRoot(String)
     * @see RegistroController
     */
    @FXML
    private void handleRegistrarse() {
        try {
            App.setRoot("registro");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la ventana de registro.");
        }
    }

    /**
     * Muestra una alerta informativa al usuario.
     * 
     * Método utilitario que crea y presenta un diálogo modal con información
     * para el usuario. Utilizado principalmente para notificar errores de
     * autenticación o problemas de navegación entre vistas.
     * 
     * @param titulo el título que aparecerá en la barra del diálogo
     * @param contenido el mensaje principal que verá el usuario
     */
    private void mostrarAlerta(String titulo, String contenido) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
