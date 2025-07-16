package com.example.controlador;

import com.example.modelo.*;
import com.example.servicio.EventoService;
import com.example.util.BuscarPersonaModalHelper;
import jakarta.persistence.EntityManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
        panelCamposEspecificos.getChildren().add(chkHayCharlas);
    }

    @FXML
    public void handleGuardarEvento() {
        try {
            String nombre = txtNombre.getText();
            LocalDate fechaInicio = dateFechaInicio.getValue();
            int duracion = Integer.parseInt(txtDuracion.getText());
            EstadoEvento estado = cmbEstado.getValue();
            String tipo = cmbTipoEvento.getValue();

            Evento evento;

            switch (tipo) {
                case "Feria" -> {
                    Feria feria = new Feria();
                    feria.setCantidadDeStand(Integer.parseInt(txtCantidadStands.getText()));
                    feria.setAlAirelibre(chkAlAireLibre.isSelected());
                    evento = feria;
                }
                case "Concierto" -> {
                    Concierto concierto = new Concierto();
                    concierto.setArtistas(artistasSeleccionados);
                    concierto.setEntradaGratuita(chkEntradaGratuita.isSelected());
                    evento = concierto;
                }
                case "Exposicion" -> {
                    Exposicion exposicion = new Exposicion();
                    exposicion.setTipoArte(txtTipoArte.getText());
                    exposicion.setCurador(curadorSeleccionado);
                    evento = exposicion;
                }
                case "Taller" -> {
                    Taller taller = new Taller();
                    taller.setCupoMaximo(Integer.parseInt(txtCupoMaximo.getText()));
                    taller.setInstructor(instructorSeleccionado);
                    taller.setModalidad(cmbModalidad.getValue());
                    evento = taller;
                }
                case "CicloDeCine" -> {
                    CicloDeCine ciclo = new CicloDeCine();
                    ciclo.setHayCharlas(chkHayCharlas.isSelected());
                    evento = ciclo;
                }
                default -> throw new IllegalStateException("Tipo de evento no soportado");
            }

            evento.setNombre(nombre);
            evento.setFechaInicio(fechaInicio);
            evento.setDuraciónDias(duracion);
            evento.setEstado(estado);
            evento.setResponsables(FXCollections.observableArrayList(personaLogueada));

            eventoService.guardarEvento(evento);
            System.out.println("Evento guardado: " + evento.getNombre());
            limpiarCampos();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error al guardar evento: " + ex.getMessage());
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
}
