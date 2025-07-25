package com.example.controlador;

import com.example.modelo.Persona;
import com.example.servicio.PersonaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class BuscarPersonaController {

    @FXML
    private TextField txtFiltro;

    @FXML
    private Button btnBuscar;
    
    @FXML
    private Button btnLimpiar;

    @FXML
    private ListView<Persona> listResultados;

    @FXML
    private Button btnSeleccionar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnRegistrarNueva;
    
    @FXML
    private Label lblSubtitulo;
    
    @FXML
    private Label lblResultadosInfo;
    
    @FXML
    private Label lblContadorResultados;

    private final PersonaService personaService;

    // 👇 Para saber si es simple o múltiple
    private boolean multiple = false;

    // 👇 Para devolver resultados
    private Persona seleccionada;
    private List<Persona> seleccionadas = new ArrayList<>();
    
    // 👇 Para filtrado en tiempo real
    private List<Persona> todasLasPersonas = new ArrayList<>();
    private javafx.collections.ObservableList<Persona> personasFiltradas;

    public BuscarPersonaController() {
        this.personaService = new PersonaService();
    }

    public void initialize() {
        // Configurar ListView primero
        configurarListView();
        
        // Inicializar la ObservableList
        personasFiltradas = FXCollections.observableArrayList();
        listResultados.setItems(personasFiltradas);
        
        // Cargar todas las personas
        cargarTodasLasPersonas();
        
        // Configurar eventos de botones
        btnBuscar.setOnAction(e -> aplicarFiltro());
        btnLimpiar.setOnAction(e -> limpiar());
        btnSeleccionar.setOnAction(e -> seleccionar());
        btnCancelar.setOnAction(e -> cerrar());
        btnRegistrarNueva.setOnAction(e -> registrarNuevaPersona());
        
        // Filtrado en tiempo real
        txtFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
            aplicarFiltro();
        });
        
        // Estado inicial
        btnSeleccionar.setDisable(true);
    }

    private void configurarListView() {
        // Configurar cómo se muestra cada persona en la lista
        listResultados.setCellFactory(param -> new ListCell<Persona>() {
            @Override
            protected void updateItem(Persona persona, boolean empty) {
                super.updateItem(persona, empty);
                if (empty || persona == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    System.out.println("Celda vacía o null");
                } else {
                    // Verificar que los datos existen
                    String nombre = persona.getNombre() != null ? persona.getNombre() : "SIN_NOMBRE";
                    String apellido = persona.getApellido() != null ? persona.getApellido() : "SIN_APELLIDO";
                    int dni = persona.getDni();
                    
                    String texto = String.format("👤 %s %s (DNI: %s)", nombre, apellido, dni);
                    setText(texto);
                    setStyle("-fx-padding: 10; -fx-font-size: 14px; -fx-background-color: #f9f9f9; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
                    
                    System.out.println("Renderizando persona: " + texto);
                }
            }
        });
        
        // Listener para selección (habilitar/deshabilitar botón)
        listResultados.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                btnSeleccionar.setDisable(newValue == null);
            }
        );
        
        // Debug: Listener para cambios en los items
        listResultados.itemsProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("ListView items cambiaron: " + (newValue != null ? newValue.size() : "null") + " elementos");
        });
    }

    private void cargarTodasLasPersonas() {
        try {
            lblResultadosInfo.setText("Cargando personas...");
            
            // Cargar todas las personas del sistema
            todasLasPersonas = personaService.obtenerTodas();
            
            // Debug: Verificar que se cargaron correctamente
            System.out.println("Cargadas " + todasLasPersonas.size() + " personas en total");
            if (!todasLasPersonas.isEmpty()) {
                System.out.println("Primera persona: " + todasLasPersonas.get(0).getNombre() + " " + todasLasPersonas.get(0).getApellido());
            }
            
            // Actualizar la ObservableList existente
            personasFiltradas.setAll(todasLasPersonas);
            
            // Debug: Verificar que la ObservableList se actualizó
            System.out.println("personasFiltradas.size(): " + personasFiltradas.size());
            System.out.println("listResultados.getItems().size(): " + listResultados.getItems().size());
            
            // Forzar refresh del ListView
            listResultados.refresh();
            
            actualizarContadorResultados(todasLasPersonas.size());
            
            lblResultadosInfo.setText("✅ Todas las personas cargadas - Puede buscar escribiendo en el campo");
            
        } catch (Exception e) {
            lblResultadosInfo.setText("❌ Error cargando personas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void aplicarFiltro() {
        String filtro = txtFiltro.getText();
        
        if (filtro == null || filtro.trim().isEmpty()) {
            // Mostrar todas las personas
            personasFiltradas.setAll(todasLasPersonas);
            lblResultadosInfo.setText("✅ Mostrando todas las personas");
        } else {
            // Filtrar por DNI o nombre
            String filtroLower = filtro.trim().toLowerCase();
            List<Persona> resultadosFiltrados = todasLasPersonas.stream()
                .filter(persona -> 
                    String.valueOf(persona.getDni()).contains(filtro) ||
                    persona.getNombre().toLowerCase().contains(filtroLower) ||
                    persona.getApellido().toLowerCase().contains(filtroLower) ||
                    (persona.getNombre() + " " + persona.getApellido()).toLowerCase().contains(filtroLower)
                )
                .toList();
            
            personasFiltradas.setAll(resultadosFiltrados);
            
            if (resultadosFiltrados.isEmpty()) {
                lblResultadosInfo.setText("❌ No se encontraron personas que coincidan con '" + filtro + "'");
            } else {
                lblResultadosInfo.setText("✅ Filtrado por: '" + filtro + "'");
            }
        }
        
        actualizarContadorResultados(personasFiltradas.size());
        
        // Debug
        System.out.println("Filtro aplicado: '" + filtro + "' - Resultados: " + personasFiltradas.size());
    }

    private void limpiar() {
        txtFiltro.clear();
        // Al limpiar, mostrar todas las personas nuevamente
        if (personasFiltradas != null && todasLasPersonas != null) {
            personasFiltradas.setAll(todasLasPersonas);
            actualizarContadorResultados(todasLasPersonas.size());
            lblResultadosInfo.setText("✅ Filtro limpiado - Mostrando todas las personas");
        }
    }

    private void actualizarContadorResultados(int cantidad) {
        String texto = cantidad + (cantidad == 1 ? " persona encontrada" : " personas encontradas");
        lblContadorResultados.setText(texto);
        btnSeleccionar.setDisable(cantidad == 0);
        
        // Debug
        System.out.println("Contador actualizado: " + texto);
    }

    // Permitir configurar modo múltiple desde el helper:
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
        listResultados.getSelectionModel().setSelectionMode(
                multiple ? SelectionMode.MULTIPLE : SelectionMode.SINGLE
        );
        
        // Actualizar textos según el modo
        if (multiple) {
            lblSubtitulo.setText("Seleccionar múltiples personas (Ctrl+Click para selección múltiple)");
            btnSeleccionar.setText("✅ Seleccionar Personas");
        } else {
            lblSubtitulo.setText("Buscar por DNI, nombre o apellido");
            btnSeleccionar.setText("✅ Seleccionar Persona");
        }
    }

    private void seleccionar() {
        if (multiple) {
            List<Persona> seleccion = listResultados.getSelectionModel().getSelectedItems();
            if (seleccion.isEmpty()) {
                mostrarAlerta("Debe seleccionar al menos una persona.");
                return;
            }
            seleccionadas = new ArrayList<>(seleccion);
        } else {
            Persona p = listResultados.getSelectionModel().getSelectedItem();
            if (p == null) {
                mostrarAlerta("Debe seleccionar una persona.");
                return;
            }
            seleccionada = p;
        }
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public Persona getSeleccionada() {
        return seleccionada;
    }

    public List<Persona> getSeleccionadas() {
        return seleccionadas;
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void registrarNuevaPersona() {
        try {
            // Cargar el formulario de registro existente
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/example/registro.fxml")
            );
            javafx.scene.Parent root = loader.load();

            RegistroController registroController = loader.getController();
            
            // Configurar el controller en modo modal (sin usuario/contraseña)
            registroController.setModoModal(true);

            // Crear y mostrar el modal
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Registrar Nueva Persona");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            // Obtener la persona registrada
            Persona nuevaPersona = registroController.getPersonaRegistrada();
            if (nuevaPersona != null) {
                // Seleccionar automáticamente la persona recién creada
                if (multiple) {
                    seleccionadas.clear();
                    seleccionadas.add(nuevaPersona);
                } else {
                    seleccionada = nuevaPersona;
                }
                // Cerrar el modal principal
                cerrar();
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al abrir el formulario de registro: " + e.getMessage());
        }
    }
}
