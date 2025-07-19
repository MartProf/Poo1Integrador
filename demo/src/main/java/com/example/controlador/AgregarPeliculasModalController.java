package com.example.controlador;

import com.example.modelo.Pelicula;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AgregarPeliculasModalController {

    @FXML private TextField tituloField;
    @FXML private TextField ordenField;
    @FXML private TableView<Pelicula> tablaPeliculas;
    @FXML private TableColumn<Pelicula, String> colTitulo;
    @FXML private TableColumn<Pelicula, Integer> colOrden;

    private ObservableList<Pelicula> listaPeliculas = FXCollections.observableArrayList();

    private NuevoEventoController controladorPadre;

    public void setControladorPadre(NuevoEventoController padre) {
        this.controladorPadre = padre;
    }

    @FXML
    public void initialize() {
        colTitulo.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitulo()));
        colOrden.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getOrden()).asObject());
        tablaPeliculas.setItems(listaPeliculas);
    }

    @FXML
    private void handleAgregarPelicula() {
        String titulo = tituloField.getText();
        String ordenText = ordenField.getText();

        if (titulo.isEmpty() || ordenText.isEmpty()) {
            mostrarAlerta("Debe completar ambos campos.");
            return;
        }

        int orden;
        try {
            orden = Integer.parseInt(ordenText);
        } catch (NumberFormatException e) {
            mostrarAlerta("El orden debe ser un número.");
            return;
        }

        Pelicula peli = new Pelicula();
        peli.setTitulo(titulo);
        peli.setOrden(orden);

        listaPeliculas.add(peli);
        tituloField.clear();
        ordenField.clear();
    }

    @FXML
    private void handleEliminarSeleccionada() {
        Pelicula seleccionada = tablaPeliculas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            listaPeliculas.remove(seleccionada);
        } else {
            mostrarAlerta("Debe seleccionar una película para eliminar.");
        }
    }

    @FXML
    private void handleCancelar() {
        ((Stage) tituloField.getScene().getWindow()).close();
    }

    @FXML
    private void handleAceptar() {
        controladorPadre.setPeliculasAgregadas(listaPeliculas);
        ((Stage) tituloField.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atención");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
