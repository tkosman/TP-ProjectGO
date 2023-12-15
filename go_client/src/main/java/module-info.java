module com.go_game.client {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.go_game.client to javafx.fxml;
    exports com.go_game.client;
}
