package my.tetris;

import javafx.application.Application;
import javafx.stage.Stage;
import my.tetris.gui.Gui;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Gui gui = new Gui(stage);

        Controller controller = new Controller(gui);

        gui.setController(controller);

        gui.launch();
    }

    public static void main(String[] args) {
        launch();
    }

}