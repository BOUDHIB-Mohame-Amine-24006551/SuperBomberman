package fr.univ.bomberman.tools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

import java.io.IOException;

public class LevelEditorView extends Application{
    private Stage stage;
    private Scene scene;

    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/univ/bomberman/fxml/level/level_editor.fxml"));
            Parent root = loader.load();
            
            scene = new Scene(root);
            stage = new Stage();
            stage.setTitle("Éditeur de Niveau - Super Bomberman");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 