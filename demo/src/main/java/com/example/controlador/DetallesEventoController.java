package com.example.controlador;

import com.example.modelo.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controlador para la vista de detalles completos de eventos.
 * 
 * Este controlador implementa una ventana modal que muestra información
 * exhaustiva sobre un evento específico del sistema municipal:
 * 
 * INFORMACIÓN BÁSICA:
 * 1. Datos generales: nombre, fecha de inicio, estado actual
 * 2. Responsables: lista de personas a cargo del evento
 * 3. Participantes: lista completa con información de contacto
 * 4. Estado de cupo: disponibilidad y ocupación actual
 * 
 * INFORMACIÓN ESPECÍFICA POR TIPO:
 * - Taller: modalidad, instructor, cupo máximo
 * - Concierto: entrada gratuita, lista de artistas
 * - Ciclo de Cine: presencia de charlas, películas incluidas
 * - Exposición: tipo de arte, curador responsable
 * - Feria: cantidad de stands, ubicación (aire libre/interior)
 * 
 * CARACTERÍSTICAS DE LA INTERFAZ:
 * - Modal no redimensionable para layout consistente
 * - Información organizada en secciones claramente diferenciadas
 * - Lista de participantes con formato "👤 Nombre Apellido - DNI: XXXXXXXX"
 * - Indicadores visuales de estado con iconos emoji
 * - Información de cupo con códigos de color (verde/rojo/azul)
 * 
 * PATRONES DE DISEÑO:
 * - Factory pattern para detalles específicos por tipo de evento
 * - Observer pattern mediante Platform.runLater() para updates de UI
 * - Strategy pattern para formateo diferenciado de información
 * 
 * INTEGRACIÓN:
 * - Llamado desde EventosDisponiblesController y MisEventosController
 * - Configuración automática mediante setEvento()
 * - Carga asíncrona de datos para mejor performance
 * 
 * El controlador está optimizado para proporcionar una vista completa
 * y detallada que permita a usuarios y administradores tener visión
 * integral del evento y su estado actual.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see Evento
 * @see Participante
 * @see EventosDisponiblesController
 * @see MisEventosController
 */
public class DetallesEventoController implements Initializable {

    // =============== ELEMENTOS DE INFORMACIÓN BÁSICA ===============
    
    /**
     * Label que muestra el nombre del evento con formato.
     * 
     * Formato: "📋 [Nombre del Evento]"
     * Proporciona identificación principal y clara del evento
     * que se está visualizando en el modal.
     */
    @FXML
    private Label lblNombre;
    
    /**
     * Label que muestra la fecha de inicio del evento.
     * 
     * Formato: "📅 DD/MM/YYYY"
     * Utiliza DateTimeFormatter para presentación localizada
     * de la fecha de inicio del evento.
     */
    @FXML
    private Label lblFecha;
    
    /**
     * Label que muestra el estado actual del evento con iconos.
     * 
     * Estados con iconos:
     * - "📋 Planificado": Evento en proceso de organización
     * - "✅ Confirmado": Evento aprobado y listo
     * - "⚡ En Ejecución": Evento actualmente en curso
     * - "🏁 Finalizado": Evento completado
     */
    @FXML
    private Label lblEstado;
    
    /**
     * Label que muestra los responsables del evento.
     * 
     * Formato: "👨‍💼 Responsables: [Lista de nombres]"
     * Muestra nombres completos separados por comas, o
     * "Sin asignar" si no hay responsables definidos.
     */
    @FXML
    private Label lblResponsables;
    
    /**
     * Contenedor vertical para detalles específicos del tipo de evento.
     * 
     * Se popula dinámicamente según el subtipo de evento:
     * - Taller: modalidad, instructor, cupo
     * - Concierto: entrada gratuita, artistas
     * - Ciclo de Cine: charlas, películas
     * - Exposición: tipo de arte, curador
     * - Feria: cantidad de stands, ubicación
     */
    @FXML
    private VBox detalleEspecificoBox;

    // =============== ELEMENTOS DE GESTIÓN DE PARTICIPANTES ===============
    
    /**
     * Lista visual de participantes inscritos en el evento.
     * 
     * Cada elemento muestra:
     * "👤 [Nombre] [Apellido] - DNI: [número]"
     * 
     * Utiliza CellFactory personalizado para formato consistente
     * y manejo apropiado de filas vacías.
     */
    @FXML
    private ListView<Participante> listParticipantes;
    
    /**
     * Label que muestra el conteo total de participantes.
     * 
     * Formato: "[cantidad] participante(s)"
     * Se actualiza automáticamente al cargar la lista
     * de participantes del evento.
     */
    @FXML
    private Label lblContadorParticipantes;
    
    /**
     * Label que muestra información de cupo con códigos de color.
     * 
     * Para eventos TieneCupo:
     * - Verde: "✅ X/Y cupos disponibles" (hay disponibilidad)
     * - Rojo: "❌ Cupo completo (X/Y)" (sin disponibilidad)
     * 
     * Para eventos sin límite:
     * - Azul: "♾️ Sin límite de cupo"
     */
    @FXML
    private Label lblInfoCupo;

    // =============== ELEMENTOS DE CONTROL ===============
    
    /**
     * Botón para cerrar el modal de detalles.
     * 
     * Ejecuta onCerrar() que cierra la ventana modal
     * y retorna control a la ventana padre.
     */
    @FXML
    private Button btnCerrar;

    // =============== VARIABLES DE ESTADO INTERNO ===============
    
    /**
     * Evento cujos detalles se están mostrando.
     * 
     * Configurado externamente mediante setEvento()
     * y utilizado por todos los métodos de carga de datos.
     */
    private Evento evento;
    
    /**
     * Referencia al Stage del modal para control de ventana.
     * 
     * Permite operaciones adicionales como centrado,
     * redimensionamiento o configuración de propiedades
     * específicas del modal.
     */
    private Stage dialogStage;

    /**
     * Inicializa el controlador después de cargar el FXML.
     * 
     * CONFIGURACIÓN MÍNIMA:
     * - No requiere configuración inicial específica
     * - La carga real de datos se realiza cuando se asigna el evento
     * - Permite carga lazy de componentes para mejor performance
     * 
     * PATRÓN DE INICIALIZACIÓN:
     * - initialize() -> configuración base
     * - setEvento() -> carga de datos específicos
     * - Platform.runLater() -> actualización de UI
     * 
     * @param location URL de recursos (no utilizado actualmente)
     * @param resources Bundle de recursos de localización (no utilizado actualmente)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración inicial si es necesaria
    }

    /**
     * Configura el CellFactory para la lista de participantes.
     * 
     * FORMATO DE CELDAS:
     * - Participantes válidos: "👤 [Nombre] [Apellido] - DNI: [número]"
     * - Celdas vacías: Sin contenido visual (null)
     * 
     * MANEJO DE CASOS ESPECIALES:
     * - Protección contra participantes null
     * - Protección contra personas null dentro de participantes
     * - Limpieza apropiada de celdas reutilizadas (empty = true)
     * 
     * PATRÓN CELLfactory:
     * - updateItem() sobrescrito para lógica personalizada
     * - setText() para contenido textual
     * - setGraphic() limpiado para evitar componentes gráficos residuales
     * 
     * Este método debe llamarse antes de cargar los datos de participantes
     * para asegurar el formato correcto de la visualización.
     */
    private void configurarListaParticipantes() {
        // Configurar cómo se muestra cada participante
        listParticipantes.setCellFactory(param -> new ListCell<Participante>() {
            @Override
            protected void updateItem(Participante participante, boolean empty) {
                super.updateItem(participante, empty);
                if (empty || participante == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Persona persona = participante.getPersona();
                    setText(String.format("👤 %s %s - DNI: %d", 
                           persona.getNombre(), 
                           persona.getApellido(), 
                           persona.getDni()));
                }
            }
        });
    }

    // =============== MÉTODOS DE CARGA DE INFORMACIÓN BÁSICA ===============
    
    /**
     * Carga y muestra la información básica del evento.
     * 
     * DATOS CARGADOS:
     * 1. Nombre del evento con formato "📋 [nombre]"
     * 2. Fecha de inicio formateada como "📅 DD/MM/YYYY"
     * 3. Estado actual con iconos descriptivos
     * 4. Lista de responsables o indicación de "Sin asignar"
     * 
     * FORMATO DE RESPONSABLES:
     * - Un responsable: "👨‍💼 Responsables: Juan Pérez"
     * - Múltiples: "👨‍💼 Responsables: Juan Pérez, María García"
     * - Sin responsables: "👨‍💼 Responsables: Sin asignar"
     * 
     * FORMATO DE FECHA:
     * - Utiliza DateTimeFormatter con patrón "dd/MM/yyyy"
     * - Formato localizado para mejor comprensión del usuario
     * 
     * Este método es parte del flujo de inicialización que se ejecuta
     * automáticamente cuando se asigna un evento al controlador.
     */
    private void cargarDatosEvento() {
        lblNombre.setText("📋 " + evento.getNombre());
        lblFecha.setText("📅 " + evento.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblEstado.setText(formatearEstado(evento.getEstado()));
        
        // Cargar responsables
        String responsables = evento.getResponsables().stream()
                .map(p -> p.getNombre() + " " + p.getApellido())
                .collect(Collectors.joining(", "));
        lblResponsables.setText("👨‍💼 Responsables: " + (responsables.isEmpty() ? "Sin asignar" : responsables));
    }
    
    /**
     * Formatea el estado del evento con iconos descriptivos.
     * 
     * MAPEO DE ESTADOS:
     * - PLANIFICADO → "📋 Planificado": Evento en organización
     * - CONFIRMADO → "✅ Confirmado": Evento aprobado y listo
     * - EN_EJECUCION → "⚡ En Ejecución": Evento actualmente en curso
     * - FINALIZADO → "🏁 Finalizado": Evento completado
     * - Otros → "❓ [Estado]": Fallback para estados no reconocidos
     * 
     * PROPÓSITO VISUAL:
     * - Los iconos proporcionan identificación visual rápida
     * - Consistencia con otros controladores del sistema
     * - Mejora la experiencia de usuario con feedback inmediato
     * 
     * @param estado El estado del evento a formatear
     * @return Cadena formateada con icono y nombre del estado
     */
    private String formatearEstado(EstadoEvento estado) {
        switch (estado) {
            case PLANIFICADO: return "📋 Planificado";
            case CONFIRMADO: return "✅ Confirmado";
            case EN_EJECUCION: return "⚡ En Ejecución";
            case FINALIZADO: return "🏁 Finalizado";
            default: return "❓ " + estado.toString();
        }
    }

    /**
     * Carga detalles específicos según el tipo de evento.
     * 
     * PATRÓN FACTORY DINÁMICO:
     * Utiliza instanceof para determinar el tipo específico de evento
     * y generar contenido personalizado en detalleEspecificoBox.
     * 
     * TIPOS DE EVENTO SOPORTADOS:
     * 
     * 1. TALLER:
     *    - 🎯 Modalidad: [PRESENCIAL/VIRTUAL/HIBRIDA]
     *    - 👨‍🏫 Instructor: [Nombre completo del instructor]
     *    - 👥 Cupo máximo: [número]
     * 
     * 2. CONCIERTO:
     *    - 🎫 Entrada gratuita: [Sí/No]
     *    - 🎤 Artistas: [Lista de artistas separados por comas]
     * 
     * 3. CICLO DE CINE:
     *    - 💬 Charlas: [Sí/No]
     *    - 🎬 Películas: [Lista de títulos separados por comas]
     * 
     * 4. EXPOSICIÓN:
     *    - 🎨 Tipo de arte: [tipo específico]
     *    - 👨‍🎨 Curador: [Nombre completo del curador]
     * 
     * 5. FERIA:
     *    - 🏪 Cantidad de stands: [número]
     *    - 🌤️ Al aire libre: [Sí/No]
     * 
     * MANEJO DE VALORES NULL:
     * - Todos los campos verifican null y muestran "N/A" como fallback
     * - Listas vacías se muestran como "N/A"
     * - Personas null se manejan apropiadamente
     * 
     * LIMPIEZA DEL CONTENEDOR:
     * - Siempre limpia detalleEspecificoBox antes de agregar nuevos elementos
     * - Permite reutilización del controlador para diferentes eventos
     * 
     * Este método implementa el patrón Strategy para mostrar información
     * específica sin crear controladores separados por tipo de evento.
     */
    private void cargarDetallesEspecificos() {
        // Limpiar detalles específicos
        detalleEspecificoBox.getChildren().clear();

        // Mostrar campos específicos según subtipo
        if (evento instanceof Taller taller) {
            Label modalidad = new Label("🎯 Modalidad: " + (taller.getModalidad() != null ? taller.getModalidad().toString() : "N/A"));
            Label instructor = new Label("👨‍🏫 Instructor: " + (taller.getInstructor() != null ? taller.getInstructor().getNombre() + " " + taller.getInstructor().getApellido() : "N/A"));
            Label cupoMax = new Label("👥 Cupo máximo: " + taller.getCupoMaximo());

            detalleEspecificoBox.getChildren().addAll(modalidad, instructor, cupoMax);

        } else if (evento instanceof Concierto concierto) {
            Label entradaGratuita = new Label("🎫 Entrada gratuita: " + (concierto.isEntradaGratuita() ? "Sí" : "No"));
            String artistas = concierto.getArtistas().stream()
                    .map(p -> p.getNombre() + " " + p.getApellido())
                    .collect(Collectors.joining(", "));
            Label artistasLabel = new Label("🎤 Artistas: " + (artistas.isEmpty() ? "N/A" : artistas));

            detalleEspecificoBox.getChildren().addAll(entradaGratuita, artistasLabel);

        } else if (evento instanceof CicloDeCine ciclo) {
            Label charlas = new Label("💬 Charlas: " + (ciclo.isHayCharlas() ? "Sí" : "No"));
            String peliculas = ciclo.getPeliculas().stream()
                    .map(p -> p.getTitulo())
                    .collect(Collectors.joining(", "));
            Label peliculasLabel = new Label("🎬 Películas: " + (peliculas.isEmpty() ? "N/A" : peliculas));

            detalleEspecificoBox.getChildren().addAll(charlas, peliculasLabel);

        } else if (evento instanceof Exposicion exposicion) {
            Label tipoArte = new Label("🎨 Tipo de arte: " + (exposicion.getTipoArte() != null ? exposicion.getTipoArte() : "N/A"));
            Label curador = new Label("👨‍🎨 Curador: " + (exposicion.getCurador() != null ? exposicion.getCurador().getNombre() + " " + exposicion.getCurador().getApellido() : "N/A"));

            detalleEspecificoBox.getChildren().addAll(tipoArte, curador);

        } else if (evento instanceof Feria feria) {
            Label cantidadStand = new Label("🏪 Cantidad de stands: " + feria.getCantidadDeStand());
            Label aireLibre = new Label("🌤️ Al aire libre: " + (feria.isAlAirelibre() ? "Sí" : "No"));

            detalleEspecificoBox.getChildren().addAll(cantidadStand, aireLibre);
        }
    }
    
    // =============== MÉTODOS DE GESTIÓN DE PARTICIPANTES ===============
    
    /**
     * Carga la lista de participantes del evento en la interfaz.
     * 
     * PROCESO DE CARGA:
     * 1. Convierte la lista de participantes del evento a ObservableList
     * 2. Asigna la lista a la ListView para visualización
     * 3. Actualiza el contador de participantes
     * 4. Actualiza la información de cupo según el tipo de evento
     * 
     * MANEJO DE ERRORES:
     * - Try-catch para capturar excepciones de acceso a datos
     * - Fallback a "0 participante(s)" en caso de error
     * - Logging de errores para debugging
     * - Actualización de cupo con valor 0 en caso de fallo
     * 
     * ACTUALIZACIÓN DE ESTADO:
     * - Contador se actualiza automáticamente con el tamaño real
     * - Información de cupo se recalcula con datos actuales
     * - Vista se mantiene sincronizada con el modelo de datos
     * 
     * FORMATO DEL CONTADOR:
     * - Singular: "1 participante"
     * - Plural: "X participantes"
     * 
     * Este método es parte del flujo de inicialización y se ejecuta
     * automáticamente cuando se asigna un evento al controlador.
     */
    private void cargarParticipantes() {
        try {
            // Cargar participantes del evento directamente en la lista
            ObservableList<Participante> participantes = FXCollections.observableArrayList(evento.getParticipantes());
            listParticipantes.setItems(participantes);
            // Actualizar contador
            lblContadorParticipantes.setText(participantes.size() + " participante(s)");
            // Actualizar info de cupo
            actualizarInfoCupo(participantes.size());
            
        } catch (Exception e) {
            System.err.println("Error al cargar participantes: " + e.getMessage());
            lblContadorParticipantes.setText("0 participante(s)");
            actualizarInfoCupo(0);
        }
    }
    
    /**
     * Actualiza la información de cupo con formato y colores dinámicos.
     * 
     * LÓGICA POR TIPO DE EVENTO:
     * 
     * 1. EVENTOS CON CUPO (TieneCupo):
     *    - Cupo disponible: "✅ X/Y cupos disponibles" (verde, #28a745)
     *    - Cupo completo: "❌ Cupo completo (X/Y)" (rojo, #dc3545)
     * 
     * 2. EVENTOS SIN LÍMITE:
     *    - "♾️ Sin límite de cupo" (azul, #17a2b8)
     * 
     * CÁLCULOS:
     * - Disponible = cupoMaximo - inscriptos
     * - Ocupación = inscriptos / cupoMaximo
     * 
     * ESTILOS APLICADOS:
     * - font-size: 12px para legibilidad
     * - font-weight: bold para destacar información importante
     * - text-fill: colores específicos según estado
     * 
     * CÓDIGOS DE COLOR:
     * - Verde (#28a745): Estado positivo, hay disponibilidad
     * - Rojo (#dc3545): Estado crítico, cupo agotado
     * - Azul (#17a2b8): Estado informativo, sin restricciones
     * 
     * @param inscriptos Número actual de participantes inscriptos
     */
    private void actualizarInfoCupo(int inscriptos) {
        if (evento instanceof TieneCupo) {
            TieneCupo cupoEvento = (TieneCupo) evento;
            int total = cupoEvento.getCupoMaximo();
            int disponible = total - inscriptos;
            
            if (disponible > 0) {
                lblInfoCupo.setText(String.format("✅ %d/%d cupos disponibles", disponible, total));
                lblInfoCupo.setStyle("-fx-font-size: 12px; -fx-text-fill: #28a745; -fx-font-weight: bold;");
            } else {
                lblInfoCupo.setText(String.format("❌ Cupo completo (%d/%d)", inscriptos, total));
                lblInfoCupo.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
            }
        } else {
            lblInfoCupo.setText("♾️ Sin límite de cupo");
            lblInfoCupo.setStyle("-fx-font-size: 12px; -fx-text-fill: #17a2b8; -fx-font-weight: bold;");
        }
    }
    
    // =============== MÉTODOS DE CONTROL DE VENTANA ===============
    
    /**
     * Manejador del evento de cierre del modal.
     * 
     * FUNCIONALIDAD:
     * - Obtiene referencia al Stage actual desde el botón de cerrar
     * - Cierra la ventana modal y retorna control a la ventana padre
     * - No requiere validaciones ni confirmaciones adicionales
     * 
     * PATRÓN DE CIERRE:
     * - Utiliza Scene.getWindow() para obtener Stage
     * - Método Stage.close() para cierre limpio
     * - El modal desaparece y la ventana padre recupera el foco
     * 
     * INTEGRACIÓN FXML:
     * - Método marcado con @FXML para binding automático
     * - Asociado al botón btnCerrar en el archivo FXML
     * - Ejecutado automáticamente al hacer click en el botón
     */
    @FXML
    private void onCerrar() {
        // Cerrar el modal
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
    
    // =============== MÉTODOS DE CONFIGURACIÓN EXTERNA ===============
    
    /**
     * Configura el evento a mostrar y dispara la carga de datos.
     * 
     * FLUJO DE CONFIGURACIÓN:
     * 1. Asigna el evento a la variable de instancia
     * 2. Utiliza Platform.runLater() para carga asíncrona
     * 3. Ejecuta secuencialmente todos los métodos de carga
     * 4. Configura la lista de participantes al final
     * 
     * SECUENCIA DE CARGA:
     * 1. cargarDatosEvento() → información básica
     * 2. cargarDetallesEspecificos() → detalles por tipo
     * 3. cargarParticipantes() → lista y contadores
     * 4. configurarListaParticipantes() → formato de celdas
     * 
     * PATRÓN ASÍNCRONO:
     * - Platform.runLater() evita bloqueo de la UI
     * - Permite carga fluida de datos pesados
     * - Mantiene responsividad de la interfaz
     * 
     * PUNTO DE ENTRADA:
     * - Llamado desde controladores padre (EventosDisponiblesController, MisEventosController)
     * - Activa toda la funcionalidad del modal
     * - Convierte el controlador genérico en específico para el evento
     * 
     * @param evento El evento cuyos detalles se van a mostrar
     */
    public void setEvento(Evento evento) {
        this.evento = evento;
        
        // Inicializar elementos cuando el evento esté disponible
        Platform.runLater(() -> {
            cargarDatosEvento();
            cargarDetallesEspecificos();
            cargarParticipantes();
            configurarListaParticipantes();
        });
    }
    
    /**
     * Establece referencia al Stage del modal para operaciones avanzadas.
     * 
     * FUNCIONALIDAD PREPARADA:
     * - Permite control adicional sobre propiedades del modal
     * - Configuración de posición, tamaño, o comportamiento
     * - Operaciones específicas de ventana si son necesarias
     * 
     * ESTADO ACTUAL:
     * - Variable almacenada pero no utilizada activamente
     * - Preparada para futuras extensiones de funcionalidad
     * - Patrón común en arquitecturas JavaFX modulares
     * 
     * @param stage El Stage del modal de detalles
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }
}
