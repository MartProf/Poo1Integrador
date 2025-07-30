package com.example.controlador;

import com.example.App;
import com.example.modelo.Persona;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;

/**
 * Controlador principal del dashboard del sistema municipal de eventos.
 * 
 * Este controlador actúa como hub central de navegación que permite
 * acceder a todas las funcionalidades principales del sistema:
 * 
 * FUNCIONALIDADES DE NAVEGACIÓN:
 * 1. Eventos Disponibles: Vista ciudadana de eventos para inscripción
 * 2. Mis Eventos: Gestión municipal de eventos propios
 * 3. Nuevo Evento: Creación y edición de eventos
 * 4. Perfil: Gestión de datos personales e inscripciones
 * 5. Cerrar Sesión: Logout y retorno a pantalla de autenticación
 * 
 * ARQUITECTURA DE CONTENEDOR:
 * - Utiliza StackPane como contenedor principal para vistas dinámicas
 * - Carga FXML dinámicamente mediante FXMLLoader
 * - Mantiene barra de navegación fija con contenido intercambiable
 * - Propaga persona logueada a todos los controladores hijos
 * 
 * PATRÓN DE DISEÑO:
 * - Controller Facade: Centraliza navegación y gestión de sesión
 * - Single Page Application: Una ventana con contenido dinámico
 * - Dependency Injection: Pasa persona logueada a controladores
 * 
 * FLUJO DE TRABAJO:
 * 1. SesionController autentica usuario y establece personaLogueada
 * 2. Dashboard muestra vista por defecto (EventosDisponibles)
 * 3. Usuario navega entre secciones mediante botones de la barra
 * 4. Cada vista recibe contexto de usuario autenticado
 * 5. Cierre de sesión retorna a pantalla de login
 * 
 * CARACTERÍSTICAS DE INTERFAZ:
 * - Barra superior con nombre de usuario logueado
 * - Botones de navegación siempre visibles
 * - Área de contenido principal que cambia dinámicamente
 * - Feedback visual de sección activa
 * 
 * GESTIÓN DE SESIÓN:
 * - Mantiene referencia a persona autenticada
 * - Propaga información de sesión a todas las vistas
 * - Permite logout limpio con retorno a pantalla inicial
 * 
 * El controlador está diseñado para ser el punto central de la
 * experiencia post-autenticación, proporcionando acceso fluido
 * a todas las funcionalidades del sistema municipal.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see SesionController
 * @see EventosDisponiblesController
 * @see MisEventosController
 * @see NuevoEventoController
 * @see PerfilController
 */
public class DashboardController {

    // =============== COMPONENTES DE INTERFAZ ===============
    
    /**
     * Contenedor principal para las vistas dinámicas del dashboard.
     * 
     * StackPane que actúa como área de contenido intercambiable:
     * - Permite cargar diferentes vistas FXML dinámicamente
     * - Mantiene solo una vista activa a la vez
     * - Ocupa toda el área disponible debajo de la barra de navegación
     * - Se limpia y reemplaza completamente en cada navegación
     * 
     * Vistas que se cargan en este contenedor:
     * - eventos_disponibles.fxml (por defecto)
     * - misEventos.fxml
     * - nuevoEvento.fxml
     * - perfil.fxml
     */
    @FXML
    private StackPane contentPane;

    /**
     * Label que muestra información del usuario autenticado.
     * 
     * Formato: "[Nombre] [Apellido]"
     * - Se actualiza automáticamente cuando se establece la persona logueada
     * - Proporciona contexto visual de quién está usando el sistema
     * - Ubicado en la barra superior del dashboard
     * - Actualización protegida contra valores null
     */
    @FXML
    private Label lblUsuario;

    // =============== VARIABLES DE ESTADO ===============
    
    /**
     * Persona autenticada actualmente en el sistema.
     * 
     * Esta referencia es fundamental para:
     * - Mostrar información personalizada en la interfaz
     * - Propagar contexto de usuario a controladores hijos
     * - Validar permisos y accesos según el usuario
     * - Mantener coherencia de sesión en toda la aplicación
     * 
     * Se establece desde SesionController tras autenticación exitosa
     * y se propaga a cada controlador hijo al navegar.
     */
    private Persona personaLogueada;

    /**
     * Inicializa el dashboard después de cargar el FXML.
     * 
     * CONFIGURACIÓN INICIAL:
     * - Carga la vista por defecto (EventosDisponibles)
     * - Establece el estado inicial del contenedor principal
     * - Prepara la interfaz para recibir información de usuario
     * 
     * VISTA POR DEFECTO:
     * - EventosDisponibles se considera la vista principal
     * - Apropiada tanto para ciudadanos como para administradores
     * - Proporciona acceso inmediato a funcionalidad principal
     * 
     * Se ejecuta automáticamente por JavaFX antes de mostrar la ventana.
     */
    @FXML
    public void initialize() {
        mostrarVistaEventosDisponibles();
    }

    // =============== MÉTODOS DE CONFIGURACIÓN DE SESIÓN ===============
    
    /**
     * Establece la persona autenticada y actualiza la interfaz.
     * 
     * PROCESO DE CONFIGURACIÓN:
     * 1. Almacena referencia a la persona logueada
     * 2. Actualiza el label de usuario en la barra superior
     * 3. Valida que tanto el label como la persona no sean null
     * 
     * FORMATO DE VISUALIZACIÓN:
     * - "[Nombre] [Apellido]" en lblUsuario
     * - Información visible en todo momento en la barra superior
     * - Proporciona contexto constante de sesión activa
     * 
     * PROTECCIÓN CONTRA NULL:
     * - Verifica que lblUsuario esté inicializado (post-FXML loading)
     * - Verifica que persona no sea null antes de extraer datos
     * - Evita NullPointerException en actualizaciones de UI
     * 
     * INTEGRACIÓN:
     * - Llamado desde SesionController tras autenticación exitosa
     * - Punto de entrada principal para establecer contexto de usuario
     * - Prepara dashboard para operaciones con contexto de sesión
     * 
     * @param persona La persona autenticada en el sistema
     */
    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
        if (lblUsuario != null && persona != null) {
            lblUsuario.setText(persona.getNombre() + " " + persona.getApellido());
        } 
    }

    // =============== MÉTODOS DE NAVEGACIÓN DEL DASHBOARD ===============
    
    /**
     * Navega a la vista de gestión de eventos propios.
     * 
     * FUNCIONALIDAD:
     * - Carga misEventos.fxml en el contenedor principal
     * - Configura MisEventosController con persona logueada
     * - Permite gestión municipal de eventos creados por el usuario
     * 
     * VISTA CARGADA:
     * - Tabla de eventos donde el usuario es responsable
     * - Funcionalidades de edición, cambio de estado, gestión de participantes
     * - Filtros avanzados por estado, fecha, tipo de evento
     * 
     * CONTEXTO DE USUARIO:
     * - Propaga personaLogueada al controlador hijo
     * - Permite filtrado automático por eventos del usuario
     * - Habilita funcionalidades específicas según permisos
     * 
     * MANEJO DE ERRORES:
     * - Try-catch para capturar errores de carga FXML
     * - printStackTrace() para debugging en desarrollo
     * - La vista actual se mantiene en caso de error
     */
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

    /**
     * Navega a la vista de creación/edición de eventos.
     * 
     * FUNCIONALIDAD:
     * - Carga nuevoEvento.fxml en el contenedor principal
     * - Configura NuevoEventoController con persona logueada
     * - Permite crear nuevos eventos o editar existentes
     * 
     * VISTA CARGADA:
     * - Formulario dinámico que se adapta al tipo de evento
     * - Validaciones exhaustivas de campos obligatorios
     * - Integración con modal de búsqueda de personas
     * - Gestión de responsables y configuración específica por tipo
     * 
     * CONTEXTO DE USUARIO:
     * - Usuario logueado se establece automáticamente como responsable
     * - Permisos de creación según rol del usuario
     * - Validaciones de negocio aplicadas según contexto
     * 
     * CASOS DE USO:
     * - Crear nuevo evento desde cero
     * - Editar evento existente (cuando se llama con parámetros)
     * - Duplicar evento con modificaciones
     */
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

    /**
     * Navega a la vista de gestión de perfil de usuario.
     * 
     * FUNCIONALIDAD:
     * - Carga perfil.fxml en el contenedor principal
     * - Configura PerfilController con persona logueada
     * - Permite gestión de datos personales e inscripciones
     * 
     * VISTA CARGADA:
     * - Formulario de datos personales (nombre, apellido, DNI, email)
     * - Lista de eventos donde el usuario está inscrito
     * - Funcionalidades de desinscripción de eventos
     * - Validaciones de integridad de datos personales
     * 
     * CONTEXTO DE USUARIO:
     * - Muestra datos específicos del usuario logueado
     * - Permite edición de información personal
     * - Filtra inscripciones automáticamente por usuario
     * 
     * COMENTARIO LEGACY:
     * - Línea comentada de refrescarInscripciones() indica funcionalidad
     *   que se maneja automáticamente en la inicialización del controlador
     * - Se mantiene para referencia histórica del desarrollo
     */
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

    /**
     * Cierra la sesión actual y retorna a la pantalla de autenticación.
     * 
     * PROCESO DE LOGOUT:
     * 1. Utiliza App.setRoot() para cambiar la escena principal
     * 2. Carga sesion.fxml como nueva vista raíz
     * 3. Limpia automáticamente el contexto del dashboard
     * 4. Retorna al estado inicial de la aplicación
     * 
     * LIMPIEZA DE SESIÓN:
     * - La referencia a personaLogueada se pierde al destruir el controlador
     * - El StackPane contentPane se limpia automáticamente
     * - Nuevo SesionController se inicializa desde cero
     * 
     * SEGURIDAD:
     * - No persiste información de sesión tras logout
     * - Fuerza nueva autenticación para acceso posterior
     * - Previene acceso no autorizado a funcionalidades post-logout
     * 
     * MANEJO DE ERRORES:
     * - Try-catch para capturar errores de carga de vista
     * - En caso de error, la sesión actual se mantiene activa
     * - printStackTrace() para debugging en desarrollo
     */
    @FXML
    private void handleCerrarSesion() {
        try {
            App.setRoot("sesion"); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navega a la vista de eventos disponibles (vista ciudadana).
     * 
     * FUNCIONALIDAD:
     * - Delega a mostrarVistaEventosDisponibles() para lógica común
     * - Manejador FXML para botón de navegación
     * - Permite retorno a vista principal desde cualquier sección
     * 
     * PATRÓN DE DELEGACIÓN:
     * - Utiliza método auxiliar para evitar duplicación de código
     * - Permite reutilización desde initialize() y desde navegación manual
     * - Mantiene consistencia en el proceso de carga
     */
    @FXML
    private void handleEventosDisponibles() {
        mostrarVistaEventosDisponibles();
    }

    /**
     * Carga la vista de eventos disponibles en el contenedor principal.
     * 
     * FUNCIONALIDAD:
     * - Carga eventos_disponibles.fxml en el contenedor principal
     * - Configura EventosDisponiblesController con persona logueada
     * - Vista principal para exploración e inscripción a eventos
     * 
     * VISTA CARGADA:
     * - Calendario interactivo mensual con eventos
     * - Tabla de eventos del día seleccionado
     * - Funcionalidades de inscripción ciudadana
     * - Filtrado automático de eventos disponibles
     * 
     * CONTEXTO DE USUARIO:
     * - Propaga personaLogueada para futuras funcionalidades
     * - Aunque actualmente muestra todos los eventos disponibles
     * - Preparado para personalización según usuario
     * 
     * VISTA POR DEFECTO:
     * - Llamada desde initialize() al cargar el dashboard
     * - Llamada desde handleEventosDisponibles() en navegación manual
     * - Considerada la vista "home" del sistema
     * 
     * MANEJO DE ERRORES:
     * - Try-catch estándar para errores de carga FXML
     * - printStackTrace() para debugging
     * - Mantiene vista actual en caso de fallo
     */
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
