module fr.univ.bomberman {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;

    // Exporter les packages vers JavaFX pour permettre l'accès aux contrôleurs
    exports fr.univ.bomberman.controller to javafx.fxml;
    exports fr.univ.bomberman to javafx.graphics;

    // Si vous avez d'autres packages avec des contrôleurs, exportez-les aussi
    // exports fr.univ.bomberman.model to javafx.fxml;
    // exports fr.univ.bomberman.utils to javafx.fxml;

    // Ouvrir les packages pour la réflexion (alternative plus permissive)
    opens fr.univ.bomberman.controller to javafx.fxml;
    opens fr.univ.bomberman to javafx.fxml;
}