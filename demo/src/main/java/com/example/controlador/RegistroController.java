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

/**
 * Controlador para la gestión de registro de usuarios en el sistema municipal.
 * 
 * Esta clase maneja tanto el registro completo de nuevos usuarios con credenciales
 * de acceso al sistema, como el registro simple de personas que solo necesitan
 * ser registradas como responsables, artistas o instructores sin acceso al sistema.
 * 
 * El controlador opera en dos modalidades:
 * - Modo normal: Registro completo con usuario y contraseña para acceso al sistema
 * - Modo modal: Registro simple solo con datos personales para roles específicos
 * 
 * Incluye validaciones de campos obligatorios, formato de DNI, y delegación
 * de validaciones de negocio al servicio correspondiente.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see SesionController
 * @see Persona
 * @see PersonaService
 */
public class RegistroController {

    /**
     * Campo de texto para ingresar el nombre de la persona.
     * 
     * Campo obligatorio para el registro. Debe contener el nombre
     * o nombres de pila de la persona que se está registrando.
     */
    @FXML
    private TextField txtNombre;

    /**
     * Campo de texto para ingresar el apellido de la persona.
     * 
     * Campo obligatorio para el registro. Debe contener el apellido
     * o apellidos de la persona que se está registrando.
     */
    @FXML
    private TextField txtApellido;

    /**
     * Campo de texto para ingresar el DNI de la persona.
     * 
     * Campo obligatorio que debe contener un número de DNI válido.
     * Se valida tanto el formato numérico como la unicidad en el sistema.
     */
    @FXML
    private TextField txtDni;

    /**
     * Campo de texto para ingresar el número de teléfono.
     * 
     * Campo obligatorio para contacto directo con la persona registrada.
     * Utilizado para comunicaciones urgentes y confirmaciones.
     */
    @FXML
    private TextField txtTelefono;

    /**
     * Campo de texto para ingresar la dirección de email.
     * 
     * Campo obligatorio para comunicaciones oficiales, notificaciones
     * automáticas y confirmaciones de eventos. Debe ser único en el sistema.
     */
    @FXML
    private TextField txtEmail;

    /**
     * Campo de texto para ingresar el nombre de usuario.
     * 
     * Campo obligatorio solo en modo normal. Define las credenciales
     * de acceso al sistema. En modo modal este campo se oculta.
     */
    @FXML
    private TextField txtUsuario;

    /**
     * Campo de contraseña para ingresar la clave de acceso.
     * 
     * Campo obligatorio solo en modo normal. Junto con el usuario,
     * forma las credenciales para autenticación. En modo modal se oculta.
     */
    @FXML
    private PasswordField txtContrasena;

    /**
     * Etiqueta del campo usuario para control de visibilidad.
     * 
     * Se oculta automáticamente cuando el controlador opera en modo modal,
     * ya que en ese caso no se requieren credenciales de acceso.
     */
    @FXML
    private Label lblUsuario;

    /**
     * Etiqueta del campo contraseña para control de visibilidad.
     * 
     * Se oculta automáticamente cuando el controlador opera en modo modal,
     * junto con su campo correspondiente.
     */
    @FXML
    private Label lblContrasena;

    /**
     * Indica si el controlador está operando en modo modal.
     * 
     * - false: Modo normal con registro completo (usuario + contraseña)
     * - true: Modo modal con registro simple (solo datos personales)
     */
    private boolean modoModal = false;
    
    /**
     * Referencia a la persona registrada en modo modal.
     * 
     * Almacena la instancia de la persona registrada exitosamente
     * para que pueda ser recuperada por el controlador que invocó
     * el modal de registro.
     */
    private Persona personaRegistrada = null;

    /**
     * Servicio para operaciones relacionadas con personas.
     * 
     * Maneja toda la lógica de negocio para registro, validaciones
     * y persistencia de datos de personas en el sistema.
     */
    private PersonaService personaService = new PersonaService();

    /**
     * Método de inicialización llamado automáticamente por JavaFX.
     * 
     * Se ejecuta después de que se hayan cargado e inyectado todos los
     * elementos FXML. Actualmente no implementa lógica específica pero
     * está disponible para futuras configuraciones iniciales.
     */
    public void initialize() {
        // Método que se ejecuta después de cargar el FXML
    }

    /**
     * Maneja el proceso de registro de la persona en el sistema.
     * 
     * Realiza el flujo completo de registro incluyendo:
     * 1. Validaciones de campos obligatorios según el modo
     * 2. Validación de formato de DNI
     * 3. Creación de la entidad Persona
     * 4. Delegación al servicio apropiado según el modo
     * 5. Manejo de respuesta (éxito o error)
     * 
     * En modo normal registra usuario completo con credenciales.
     * En modo modal registra solo datos personales para roles específicos.
     * 
     * @see PersonaService#registrarPersona(Persona)
     * @see PersonaService#guardarPersonaSimple(Persona)
     */
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
                
                // Usar el servicio de la instancia del controlador
                personaRegistrada = personaService.guardarPersonaSimple(persona);
                
                mostrarAlerta("Registro exitoso", "Persona registrada correctamente");
                // Cerrar modal
                Stage stage = (Stage) txtNombre.getScene().getWindow();
                stage.close();
                
            } else {
                // Modo normal: registro completo
                persona.setUsuario(txtUsuario.getText().trim());
                persona.setContrasena(txtContrasena.getText().trim());

                // Usar el servicio de la instancia del controlador
                personaService.registrarPersona(persona);

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

    /**
     * Maneja la navegación de vuelta a la ventana de inicio de sesión.
     * 
     * Cancela el proceso de registro y regresa a la vista de login.
     * Solo aplica en modo normal, ya que en modo modal se cierra
     * la ventana directamente.
     * 
     * @see App#setRoot(String)
     * @see SesionController
     */
    @FXML
    private void handleVolver() {
        try {
            App.setRoot("sesion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Muestra una alerta informativa al usuario.
     * 
     * Método utilitario que crea y presenta un diálogo modal con información
     * para el usuario. Utilizado para notificar errores de validación,
     * confirmaciones de registro exitoso, o problemas durante el proceso.
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

    /**
     * Configura el controlador para operar en modo modal.
     * 
     * En modo modal:
     * - Se ocultan los campos de usuario y contraseña
     * - Se registra solo información personal básica
     * - No se crean credenciales de acceso al sistema
     * - La ventana se cierra automáticamente tras el registro
     * 
     * Este modo se utiliza cuando se necesita registrar personas
     * para roles específicos (responsables, artistas, instructores)
     * sin darles acceso directo al sistema.
     * 
     * @param modoModal true para activar modo modal, false para modo normal
     */
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

    /**
     * Obtiene la persona registrada exitosamente en modo modal.
     * 
     * Permite al controlador que invocó el modal recuperar la instancia
     * de la persona que fue registrada, para utilizarla inmediatamente
     * (por ejemplo, asignarla como responsable de un evento).
     * 
     * @return la persona registrada en modo modal, null si no se completó
     *         el registro o si no está en modo modal
     */
    public Persona getPersonaRegistrada() {
        return personaRegistrada;
    }
}
