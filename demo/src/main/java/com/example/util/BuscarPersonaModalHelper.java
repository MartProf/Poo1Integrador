package com.example.util;

import com.example.controlador.BuscarPersonaController;
import com.example.modelo.Persona;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Collections;
import java.util.List;

public class BuscarPersonaModalHelper {

    public static Persona abrirSeleccionSimple() {
        try {
            FXMLLoader loader = new FXMLLoader(BuscarPersonaModalHelper.class.getResource("/com/example/BuscarPersona.fxml"));
            Parent root = loader.load();

            BuscarPersonaController controller = loader.getController();
            controller.setMultiple(false); // Modo simple

            Stage stage = new Stage();
            stage.setTitle("Buscar Persona");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.getSeleccionada();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Persona> abrirSeleccionMultiple() {
        try {
            FXMLLoader loader = new FXMLLoader(BuscarPersonaModalHelper.class.getResource("/com/example/BuscarPersona.fxml"));
            Parent root = loader.load();

            BuscarPersonaController controller = loader.getController();
            controller.setMultiple(true); // Modo múltiple

            Stage stage = new Stage();
            stage.setTitle("Buscar Personas");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.getSeleccionadas();

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
