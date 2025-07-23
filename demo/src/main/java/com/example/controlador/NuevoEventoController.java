package com.example.controlador;

import com.example.modelo.*;
import com.example.servicio.EventoService;
import com.example.util.BuscarPersonaModalHelper;
import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NuevoEventoController {

    @FXML
    private TextField txtNombre;

    @FXML
    private DatePicker dateFechaInicio;

    @FXML
    private TextField txtDuracion;

    @FXML
    private ComboBox<String> cmbTipoEvento;

    @FXML
    private ComboBox<EstadoEvento> cmbEstado;

    @FXML
    private VBox panelCamposEspecificos;

    // Campos específicos
    private TextField txtCantidadStands;
    private CheckBox chkAlAireLibre;

    private CheckBox chkEntradaGratuita;

    private TextField txtTipoArte;
    private Button btnBuscarCurador;
    private Label lblCuradorSeleccionado;
    private Persona curadorSeleccionado;

    private TextField txtCupoMaximo;
    private Button btnBuscarInstructor;
    private Label lblInstructorSeleccionado;
    private Persona instructorSeleccionado;

    private ComboBox<Modalidad> cmbModalidad;

    private CheckBox chkHayCharlas;

    private List<Persona> artistasSeleccionados = new ArrayList<>();
    private Button btnBuscarArtistas;
    private Label lblArtistasSeleccionados;

    private EventoService eventoService;
    private Persona personaLogueada;

    private List<Pelicula> peliculasAgregadas = new ArrayList<>();
    private Label lblPeliculasSeleccionadas;

    private boolean modoEdicion = false;
    private Evento eventoAEditar;

    public NuevoEventoController() {
        EntityManager em = com.example.util.JpaUtil.getEntityManager();
        this.eventoService = new EventoService(em);
    }

    @FXML
    public void initialize() {
        cmbTipoEvento.setItems(FXCollections.observableArrayList(
                "Feria", "Concierto", "Exposicion", "Taller", "CicloDeCine"
        ));
        cmbTipoEvento.setOnAction(e -> handleTipoEventoChanged());

        cmbEstado.setItems(FXCollections.observableArrayList(EstadoEvento.values()));
        cmbEstado.getSelectionModel().select(EstadoEvento.PLANIFICADO);
    }

    @FXML
    private void handleTipoEventoChanged() {
        String tipo = cmbTipoEvento.getValue();
        panelCamposEspecificos.getChildren().clear();

        switch (tipo) {
            case "Feria" -> mostrarCamposFeria();
            case "Concierto" -> mostrarCamposConcierto();
            case "Exposicion" -> mostrarCamposExposicion();
            case "Taller" -> mostrarCamposTaller();
            case "CicloDeCine" -> mostrarCamposCicloDeCine();
        }
    }

    private void mostrarCamposFeria() {
        txtCantidadStands = new TextField();
        chkAlAireLibre = new CheckBox("Al aire libre");

        panelCamposEspecificos.getChildren().addAll(
                new Label("Cantidad de Stands:"),
                txtCantidadStands,
                chkAlAireLibre
        );
    }

    private void mostrarCamposConcierto() {
        btnBuscarArtistas = new Button("Buscar Artistas");
        lblArtistasSeleccionados = new Label("Ningún artista seleccionado");

        btnBuscarArtistas.setOnAction(e -> {
            List<Persona> encontrados = BuscarPersonaModalHelper.abrirSeleccionMultiple();
            if (encontrados != null) {
                artistasSeleccionados.clear();
                artistasSeleccionados.addAll(encontrados);
                lblArtistasSeleccionados.setText(
                    "Seleccionados: " + artistasSeleccionados.size()
                );
            }
        });

        chkEntradaGratuita = new CheckBox("Entrada gratuita");

        panelCamposEspecificos.getChildren().addAll(
                btnBuscarArtistas,
                lblArtistasSeleccionados,
                chkEntradaGratuita
        );
    }

    private void mostrarCamposExposicion() {
        txtTipoArte = new TextField();
        btnBuscarCurador = new Button("Buscar Curador");
        lblCuradorSeleccionado = new Label("Ningún curador seleccionado");

        btnBuscarCurador.setOnAction(e -> {
            Persona p = BuscarPersonaModalHelper.abrirSeleccionSimple();
            if (p != null) {
                curadorSeleccionado = p;
                lblCuradorSeleccionado.setText(p.getNombre() + " (" + p.getDni() + ")");
            }
        });

        panelCamposEspecificos.getChildren().addAll(
                new Label("Tipo de Arte:"),
                txtTipoArte,
                btnBuscarCurador,
                lblCuradorSeleccionado
        );
    }

    private void mostrarCamposTaller() {
        txtCupoMaximo = new TextField();
        btnBuscarInstructor = new Button("Buscar Instructor");
        lblInstructorSeleccionado = new Label("Ningún instructor seleccionado");
        cmbModalidad = new ComboBox<>(FXCollections.observableArrayList(Modalidad.values()));

        btnBuscarInstructor.setOnAction(e -> {
            Persona p = BuscarPersonaModalHelper.abrirSeleccionSimple();
            if (p != null) {
                instructorSeleccionado = p;
                lblInstructorSeleccionado.setText(p.getNombre() + " (" + p.getDni() + ")");
            }
        });

        panelCamposEspecificos.getChildren().addAll(
                new Label("Cupo máximo:"),
                txtCupoMaximo,
                btnBuscarInstructor,
                lblInstructorSeleccionado,
                new Label("Modalidad:"),
                cmbModalidad
        );
    }

    private void mostrarCamposCicloDeCine() {
        chkHayCharlas = new CheckBox("¿Incluye charlas posteriores?");
        Button btnAgregarPeliculas = new Button("Agregar Películas");
        lblPeliculasSeleccionadas = new Label("0 películas seleccionadas");

        btnAgregarPeliculas.setOnAction(e -> {
            try {
                handleAgregarPeliculas();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        panelCamposEspecificos.getChildren().addAll(
            chkHayCharlas,
            btnAgregarPeliculas,
            lblPeliculasSeleccionadas
        );
    }

    @FXML
    public void handleGuardarEvento() {
        try {
            // Validaciones generales
            String nombre = txtNombre.getText();
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del evento no puede estar vacío.");
            }

            LocalDate fechaInicio = dateFechaInicio.getValue();
            if (fechaInicio == null) {
                throw new IllegalArgumentException("Debe seleccionar una fecha de inicio.");
            }

            int duracion;
            try {
                duracion = Integer.parseInt(txtDuracion.getText());
                if (duracion <= 0) {
                    throw new IllegalArgumentException("La duración debe ser un número positivo.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("La duración debe ser un número válido.");
            }

            EstadoEvento estado = cmbEstado.getValue();
            if (estado == null) {
                throw new IllegalArgumentException("Debe seleccionar un estado para el evento.");
            }

            String tipo = cmbTipoEvento.getValue();
            if (tipo == null || tipo.trim().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de evento.");
            }

            Evento evento;
            if (modoEdicion && eventoAEditar != null) {
                evento = eventoAEditar;
            } else {
                switch (tipo) {
                    case "Feria" -> evento = new Feria();
                    case "Concierto" -> evento = new Concierto();
                    case "Exposicion" -> evento = new Exposicion();
                    case "Taller" -> evento = new Taller();
                    case "CicloDeCine" -> evento = new CicloDeCine();
                    default -> throw new IllegalStateException("Tipo de evento no soportado");
                }
            }

            evento.setNombre(nombre);
            evento.setFechaInicio(fechaInicio);
            evento.setDuraciónDias(duracion);
            evento.setEstado(estado);
            mantenerResponsableEvento(evento);

            // Validaciones y asignaciones específicas por tipo
            switch (tipo) {
                case "Feria" -> {
                    int cantidad;
                    try {
                        cantidad = Integer.parseInt(txtCantidadStands.getText());
                        if (cantidad <= 0) {
                            throw new IllegalArgumentException("La cantidad de stands debe ser un número positivo.");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("La cantidad de stands debe ser un número válido.");
                    }
                    Feria feria = (Feria) evento;
                    feria.setCantidadDeStand(cantidad);
                    feria.setAlAirelibre(chkAlAireLibre.isSelected());
                }

                case "Concierto" -> {
                    if (artistasSeleccionados == null || artistasSeleccionados.isEmpty()) {
                        throw new IllegalArgumentException("Debe agregar al menos un artista al concierto.");
                    }
                    Concierto concierto = (Concierto) evento;
                    concierto.setArtistas(artistasSeleccionados);
                    concierto.setEntradaGratuita(chkEntradaGratuita.isSelected());
                }

                case "Exposicion" -> {
                    String tipoArte = txtTipoArte.getText();
                    if (tipoArte == null || tipoArte.trim().isEmpty()) {
                        throw new IllegalArgumentException("Debe ingresar el tipo de arte.");
                    }
                    if (curadorSeleccionado == null) {
                        throw new IllegalArgumentException("Debe seleccionar un curador.");
                    }
                    Exposicion exposicion = (Exposicion) evento;
                    exposicion.setTipoArte(tipoArte);
                    exposicion.setCurador(curadorSeleccionado);
                }

                case "Taller" -> {
                    int cupo;
                    try {
                        cupo = Integer.parseInt(txtCupoMaximo.getText());
                        if (cupo <= 0) {
                            throw new IllegalArgumentException("El cupo máximo debe ser un número positivo.");
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("El cupo máximo debe ser un número válido.");
                    }
                    if (instructorSeleccionado == null) {
                        throw new IllegalArgumentException("Debe seleccionar un instructor.");
                    }
                    if (cmbModalidad.getValue() == null) {
                        throw new IllegalArgumentException("Debe seleccionar una modalidad.");
                    }
                    Taller taller = (Taller) evento;
                    taller.setCupoMaximo(cupo);
                    taller.setInstructor(instructorSeleccionado);
                    taller.setModalidad(cmbModalidad.getValue());
                }

                case "CicloDeCine" -> {
                    if (peliculasAgregadas == null || peliculasAgregadas.isEmpty()) {
                        throw new IllegalArgumentException("Debe agregar al menos una película al ciclo de cine.");
                    }
                    CicloDeCine ciclo = (CicloDeCine) evento;
                    ciclo.setHayCharlas(chkHayCharlas.isSelected());
                    ciclo.setPeliculas(peliculasAgregadas);
                    for (Pelicula pelicula : peliculasAgregadas) {
                        pelicula.setCicloDeCine(ciclo);
                    }
                }
            }

            // Guardar o actualizar
            if (modoEdicion) {
                eventoService.actualizarEvento(evento);
                System.out.println("Evento actualizado: " + evento.getNombre());
            } else {
                eventoService.guardarEvento(evento);
                System.out.println("Evento guardado: " + evento.getNombre());
            }

            limpiarCampos();

        } catch (IllegalArgumentException e) {
            mostrarAlerta("Error de validación", e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "Error al guardar/actualizar evento: " + ex.getMessage());
        }
    }


    @FXML
    private void handleAgregarPeliculas() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ventanaAgregarPeliculas.fxml"));
        Parent root = loader.load();

        AgregarPeliculasModalController controller = loader.getController();
        controller.setControladorPadre(this);

        Stage stage = new Stage();
        stage.setTitle("Agregar Películas");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private void mantenerResponsableEvento(Evento evento) {
        if (!modoEdicion) {
            // En creación, se inicializa con el responsable actual
            evento.setResponsables(FXCollections.observableArrayList(personaLogueada));
        } else {
            // En edición, nos aseguramos de que la persona logueada esté entre los responsables
            if (!evento.getResponsables().contains(personaLogueada)) {
                evento.getResponsables().add(personaLogueada);
            }
        }
    }
    
    private void limpiarCampos() {
        txtNombre.clear();
        dateFechaInicio.setValue(null);
        txtDuracion.clear();
        cmbEstado.getSelectionModel().select(EstadoEvento.PLANIFICADO);
        cmbTipoEvento.getSelectionModel().clearSelection();
        panelCamposEspecificos.getChildren().clear();
    }

    public void setPersonaLogueada(Persona persona) {
        this.personaLogueada = persona;
    }

    public void setPeliculasAgregadas(List<Pelicula> peliculas) {
        if (peliculas != null) {
            this.peliculasAgregadas = peliculas;
            if (lblPeliculasSeleccionadas != null) {
                lblPeliculasSeleccionadas.setText(peliculas.size() + " película(s) seleccionada(s)");
            }
        }
    }
    
    public void setModoEdicion(boolean modoEdicion) {
        this.modoEdicion = modoEdicion;
    }

    public void cargarEventoParaEditar(Evento evento) {
        this.eventoAEditar = evento;
        this.modoEdicion = true;

        // === CAMPOS COMUNES ===
        txtNombre.setText(evento.getNombre());
        dateFechaInicio.setValue(evento.getFechaInicio());
        txtDuracion.setText(String.valueOf(evento.getDuraciónDias()));
        cmbEstado.setValue(evento.getEstado());

        // === CAMPOS ESPECIFICOS ===
        String tipo;
        if (evento instanceof Feria feria) {
            tipo = "Feria";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            txtCantidadStands.setText(String.valueOf(feria.getCantidadDeStand()));
            chkAlAireLibre.setSelected(feria.isAlAirelibre());

        } else if (evento instanceof Concierto concierto) {
            tipo = "Concierto";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            artistasSeleccionados.clear();
            artistasSeleccionados.addAll(concierto.getArtistas());
            lblArtistasSeleccionados.setText("Seleccionados: " + artistasSeleccionados.size());
            chkEntradaGratuita.setSelected(concierto.isEntradaGratuita());

        } else if (evento instanceof Exposicion exposicion) {
            tipo = "Exposicion";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            txtTipoArte.setText(exposicion.getTipoArte());
            curadorSeleccionado = exposicion.getCurador();
            lblCuradorSeleccionado.setText(curadorSeleccionado != null
                ? curadorSeleccionado.getNombre() + " (" + curadorSeleccionado.getDni() + ")"
                : "Ningún curador seleccionado");

        } else if (evento instanceof Taller taller) {
            tipo = "Taller";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            txtCupoMaximo.setText(String.valueOf(taller.getCupoMaximo()));
            instructorSeleccionado = taller.getInstructor();
            lblInstructorSeleccionado.setText(instructorSeleccionado != null
                ? instructorSeleccionado.getNombre() + " (" + instructorSeleccionado.getDni() + ")"
                : "Ningún instructor seleccionado");
            cmbModalidad.setValue(taller.getModalidad());

        } else if (evento instanceof CicloDeCine ciclo) {
            tipo = "CicloDeCine";
            cmbTipoEvento.setValue(tipo);
            handleTipoEventoChanged();
            chkHayCharlas.setSelected(ciclo.isHayCharlas());
            peliculasAgregadas = ciclo.getPeliculas();
            lblPeliculasSeleccionadas.setText(peliculasAgregadas.size() + " película(s) seleccionada(s)");
        }
    }

    //Metodo para mostrar alertas de error
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
