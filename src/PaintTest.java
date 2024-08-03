import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * This file runs an application that allows the user to draw and then save an image
 * This file DOES NOT interact with the chess engine at all, merely a test file
 */

public class PaintTest extends Application {
    
    private static final int CANVAS_DIM = 256;
    private static final int PIXEL_SIZE = 4;

    @Override
    public void start(Stage arg0) throws Exception {
        // for keeping track of which pixels have been drawed on
        boolean[][] boolBoard = new boolean[CANVAS_DIM / PIXEL_SIZE][CANVAS_DIM / PIXEL_SIZE];

        Canvas canva = new Canvas(CANVAS_DIM,CANVAS_DIM);
        GraphicsContext gc = canva.getGraphicsContext2D();
        PixelWriter pw = gc.getPixelWriter();

        VBox skeleton = new VBox();
        skeleton.getChildren().add(canva);

        StackPane root = new StackPane();
        root.getChildren().add(skeleton);

        Scene s = new Scene(root, CANVAS_DIM, CANVAS_DIM + 100);
        arg0.setScene(s);
        arg0.setTitle("Paint Test");
        arg0.show();

        // add drawing upon interaction (using some lamba expression)
        canva.setOnMouseClicked(event -> {
            int originX = (int)(event.getX()) / PIXEL_SIZE;
            int originY = (int)(event.getY()) / PIXEL_SIZE;

            Color fillColor = boolBoard[originY][originX] ? Color.WHITE : Color.BLACK;
            gc.setFill(fillColor);
            boolBoard[originY][originX] = !boolBoard[originY][originX];

            // by doing integer division and then multiplication, the clicked location
            // is now a point on a fixed grid :)
            originX *= PIXEL_SIZE;
            originY *= PIXEL_SIZE;

            System.out.println(originX + " " + originY);

            gc.fillRect(originX, originY, PIXEL_SIZE, PIXEL_SIZE);
        });

        // support to save the canvas as an image by pressing a button
        Button saveButton = new Button("Save Image");
        saveButton.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event eve) {
                FileChooser fc = new FileChooser();
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png"));
                File file = fc.showSaveDialog(arg0);

                if (file != null) {

                    try {
                        WritableImage writableImage = new WritableImage((int) canva.getWidth(), (int) canva.getHeight());
                        canva.snapshot(null, writableImage);
                        ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                    } catch (IOException ex) {
                        System.out.println("Error saving image: " + ex.getMessage());
                    }
                }
            }
            
        });
        skeleton.getChildren().add(saveButton);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
