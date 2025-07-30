package com.example;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Clase principal de la aplicación JavaFX del Sistema Municipal de Eventos.
 * 
 * Esta clase extiende de Application y actúa como punto de entrada de la
 * aplicación, manejando la inicialización de JavaFX y la gestión de escenas
 * para la navegación entre diferentes vistas del sistema.
 * 
 * La aplicación inicia con la ventana de sesión maximizada y proporciona
 * métodos estáticos para la navegación fluida entre vistas sin crear
 * nuevas ventanas.
 * 
 * @author Sistema Municipal de Eventos
 * @version 1.0
 * @since 2025-07-30
 * 
 * @see javafx.application.Application
 */
public class App extends Application {
    
    /**
     * Escena principal de la aplicación JavaFX.
     * 
     * Mantiene la referencia a la escena activa que se reutiliza
     * para todas las navegaciones entre vistas, permitiendo un
     * cambio fluido de interfaces sin crear nuevas ventanas.
     */
    private static Scene scene;

    /**
     * Método de inicialización de la aplicación JavaFX.
     * 
     * Se ejecuta automáticamente al lanzar la aplicación y realiza:
     * 1. Carga de la vista inicial de sesión
     * 2. Configuración de la ventana principal (maximizada)
     * 3. Presentación de la interfaz al usuario
     * 
     * @param stage el escenario principal proporcionado por JavaFX
     * @throws IOException si no se puede cargar el archivo FXML de sesión
     */
    @Override
    public void start(Stage stage) {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("sesion.fxml"));
            scene = new Scene(fxmlLoader.load());
            stage.setMaximized(true); // Hacer que siempre se abra maximizada
            stage.setScene(scene);
            stage.show();

        }
        catch(IOException e){
            e.printStackTrace();
        }    
    }

    /**
     * Cambia la vista actual de la aplicación.
     * 
     * Carga una nueva vista FXML y la establece como raíz de la escena
     * existente, permitiendo la navegación fluida entre diferentes
     * interfaces de usuario sin crear nuevas ventanas.
     * 
     * @param fxml el nombre del archivo FXML (sin extensión) a cargar
     * @return el FXMLLoader utilizado, permitiendo acceso al controlador
     *         de la nueva vista cargada
     * @throws IOException si el archivo FXML no existe o no se puede cargar
     */
    public static FXMLLoader setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        scene.setRoot(fxmlLoader.load());
        return fxmlLoader;
    }

    /**
     * Punto de entrada principal de la aplicación.
     * 
     * Método main estándar de Java que inicia la aplicación JavaFX
     * llamando al método launch() heredado de Application.
     * 
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main (String[] args){
            launch();
        }
    }
    