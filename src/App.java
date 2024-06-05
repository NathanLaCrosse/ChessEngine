import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public static void printBoolAr(boolean[][] ar) {
        for(int i = 0; i < ar.length; i++) {
            for(int k = 0; k < ar[0].length; k++) {
                System.out.print((ar[i][k]?1:0) + " ");
            }
            System.out.println();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ChessGame cg = new ChessGame();
        
        StackPane root = new StackPane();

        root.getChildren().add(cg.getGuiComponent());
        cg.createNewChessGame(new RandBot(true), new RandBot(false));

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}
