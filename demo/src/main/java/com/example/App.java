package com.example;

import java.io.IOException;

import com.example.servicio.PersonaService;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {
    private static Scene scene;
    private static PersonaService personaService;

    @Override
    public void start(Stage stage) {
        try{
            personaService = new PersonaService();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("sesion.fxml"));
            scene = new Scene(fxmlLoader.load());
            stage.setMaximized(true); // Hacer que siempre se abra maximizada
            stage.setScene(scene);
            stage.show();

        }
        catch(IOException e){
            System.err.println("Fallo al cargar archivo fxml: " + e.getMessage());
            e.printStackTrace();
        }    


    }

    public static PersonaService getPersonaService(){
        return personaService;
    }

    
    public static FXMLLoader setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(fxmlLoader.load());
        return fxmlLoader;
    }

    public static void main (String[] args){
            launch();
        }
    }
    