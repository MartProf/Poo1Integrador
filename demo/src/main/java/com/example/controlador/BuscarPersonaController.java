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
    private ListView<Persona> listResultados;

    @FXML
    private Button btnSeleccionar;

    @FXML
    private Button btnCancelar;

    private final PersonaService personaService;

    // 👇 Para saber si es simple o múltiple
    private boolean multiple = false;

    // 👇 Para devolver resultados
    private Persona seleccionada;
    private List<Persona> seleccionadas = new ArrayList<>();

    public BuscarPersonaController() {
        this.personaService = new PersonaService();
    }

    public void initialize() {
        btnBuscar.setOnAction(e -> buscar());
        btnSeleccionar.setOnAction(e -> seleccionar());
        btnCancelar.setOnAction(e -> cerrar());
    }

    // Permitir configurar modo múltiple desde el helper:
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
        listResultados.getSelectionModel().setSelectionMode(
                multiple ? SelectionMode.MULTIPLE : SelectionMode.SINGLE
        );
    }

    private void buscar() {
        String filtro = txtFiltro.getText().trim();
        if (filtro.isBlank()) {
            mostrarAlerta("Ingrese un nombre o DNI para buscar.");
            return;
        }

        try {
            int dni = Integer.parseInt(filtro);
            Persona p = personaService.buscarPorDni(dni);
            if (p != null) {
                listResultados.setItems(FXCollections.observableArrayList(p));
            } else {
                listResultados.setItems(FXCollections.emptyObservableList());
            }
        } catch (NumberFormatException e) {
            List<Persona> resultados = personaService.buscarPorNombre(filtro);
            listResultados.setItems(FXCollections.observableArrayList(resultados));
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
}
