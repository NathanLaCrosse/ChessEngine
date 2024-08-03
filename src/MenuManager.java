// this class manages the main GUI of athe chess program, creating the various menus and screens

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MenuManager {
    private static final String[] BOTS = new String[]{"Champion Bot", "Rand Bot", "Player", "Horrible Bot"};

    private Scene scene;
    private int[] wins;
    
    public MenuManager(Stage myStage) {
        BorderPane dummy = new BorderPane();
        scene = new Scene(dummy, 800, 600);
        
        buildMainMenu();

        myStage.setTitle("Chess Program");
        myStage.setScene(scene);
        myStage.show();
    }

    private void buildMainMenu() {
        BorderPane root = new BorderPane();

        Label title = new Label("Chess!");
        title.setStyle("-fx-font: 24 arial;");
        title.setPadding(new Insets(20, 20, 20, 120));
        root.setTop(title);

        VBox skeleton = new VBox();

        Button newGame = new Button("Create New Game");
        newGame.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event arg0) {
                buildGameSelectionScreen();
            }
            
        });

        Button pieceCreator = new Button("Create New Chess Piece");
        pieceCreator.setOnMouseClicked(event -> {
            buildPieceCreatorScreen();
        });

        skeleton.getChildren().addAll(newGame, pieceCreator);
        root.setCenter(skeleton);

        scene.setRoot(root);
    }

    private void buildGameSelectionScreen() {
        BorderPane root = new BorderPane();

        VBox playerSelect = new VBox();

        // create a combo box to select the white player
        HBox whitePlayer = new HBox();
        whitePlayer.setSpacing(20);
        Label whitePlayerLabel = new Label("Select Player 1: ");
        ComboBox<String> whitePlayerSelected = new ComboBox<>();
        whitePlayerSelected.getItems().addAll(BOTS);
        whitePlayer.getChildren().addAll(whitePlayerLabel, whitePlayerSelected);

        // create a combo box to select the black player
        HBox blackPlayer = new HBox();
        blackPlayer.setSpacing(20);
        Label blackPlayerLabel = new Label("Select Player 2: ");
        ComboBox<String> blackPlayerSelected = new ComboBox<>();
        blackPlayerSelected.getItems().addAll(BOTS);
        blackPlayer.getChildren().addAll(blackPlayerLabel, blackPlayerSelected);

        // combine into the scene
        playerSelect.getChildren().addAll(whitePlayer, blackPlayer);
        root.setCenter(playerSelect);

        Button startGame = new Button("Start Game");
        startGame.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event arg0) {
                createChessGameView(createEntityBasedOnString(whitePlayerSelected.getSelectionModel().getSelectedItem(), true), createEntityBasedOnString(blackPlayerSelected.getSelectionModel().getSelectedItem(), false));
            }
            
        });
        playerSelect.getChildren().add(startGame);

        scene.setRoot(root);
    }
    private Entity createEntityBasedOnString(String str, boolean side) {
        if(str == null) return new RandBot(side);

        switch (str) {
            case "Champion Bot":
                return new ChampionBot(side);
            case "Player":
                return new Player(side);
            case "Horrible Bot":
                return new HorribleBot(side);
            default:
                return new RandBot(side);
        }
    }

    private Label p1wins;
    private Label p2wins;
    private void createChessGameView(Entity player1, Entity player2) {
        wins = new int[]{0,0};
        BorderPane root = new BorderPane();
        ChessGame cg = new ChessGame();

        HBox layout = new HBox();

        // set up chess game
        cg.createNewChessGame(player1, player2);
        layout.getChildren().add(cg.getGuiComponent());
        RepeatGameThread rgt = new RepeatGameThread(player1, player2, cg, this);
        rgt.start();

        VBox statBlock = new VBox();

        Button exitButton = new Button("Main Menu");
        exitButton.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event arg0) {
                cg.triggerThreadFlag();
                buildMainMenu(); 
            }
            
        });

        Label s = new Label("STATS: ");
        Label p1 = new Label("Player 1 (white): " + player1.getName());
        p1wins = new Label("Wins: " + wins[0]);
        Label p2 = new Label("Player 2 (black): " + player2.getName());
        p2wins = new Label("Wins: " +  wins[1]);

        statBlock.getChildren().addAll(exitButton, s, p1, p1wins, p2, p2wins);
        layout.getChildren().add(statBlock);

        root.setLeft(layout);
        scene.setRoot(root);
    }
    public void updateWinsText() {
        p1wins.setText("Wins: " + wins[0]);
        p2wins.setText("Wins: " + wins[1]);
    }
    public void incrementWins(int sideDex) {
        wins[sideDex]++;
    }

    private void buildPieceCreatorScreen() {
        BorderPane root = new BorderPane();

        HBox skeleton = new HBox();
        VBox imageManager = new VBox();
        VBox moveAdditions = new VBox();
        skeleton.getChildren().addAll(imageManager, moveAdditions);

        Label title = new Label("Piece Creator");

        // create a canvas that the user can draw their piece's art on
        // image is 64x64 pixels
        final int PIXEL_SIZE = 3;
        Canvas canvas = new Canvas(PIXEL_SIZE*64+2, PIXEL_SIZE*64+2);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        boolean[][] boolBoard = new boolean[64][64];
        gc.strokeRect(0, 0, PIXEL_SIZE*64 + 2, PIXEL_SIZE*64 + 2);
        canvas.setOnMouseClicked(event -> {
            int originX = (int)(event.getX() - 1) / PIXEL_SIZE;
            int originY = (int)(event.getY() - 1) / PIXEL_SIZE;

            if(originX >= 0 && originX < 64 && originY >= 0 && originY < 64) {
                Color fillColor = boolBoard[originY][originX] ? Color.WHITE : Color.BLACK;
                gc.setFill(fillColor);
                boolBoard[originY][originX] = !boolBoard[originY][originX];
            }

            // by doing integer division and then multiplication, the clicked location
            // is now a point on a fixed grid :)
            originX *= PIXEL_SIZE;
            originY *= PIXEL_SIZE;

            gc.fillRect(originX + 1, originY + 1, PIXEL_SIZE, PIXEL_SIZE);
        });

        TextField pieceName = new TextField();
        pieceName.setPromptText("Enter Piece Name");

        // button to save entire piece into memory
        Button save = new Button("Save Piece");
        save.setOnMouseClicked(event -> {
            // use the values from the boolboard to write the correct pixels colors on two writable images
            // done twice for white/black variants
            WritableImage lightVariant = new WritableImage(64, 64);
            WritableImage darkVariant = new WritableImage(64, 64);
            
            PixelWriter lightPW = lightVariant.getPixelWriter();
            PixelWriter darkPW = darkVariant.getPixelWriter();

            for(int i = 0; i < 64; i++) {
                for(int j = 0; j < 64; j++) {
                    if(boolBoard[i][j]) {
                        lightPW.setColor(i, j, Color.WHITE);
                        darkPW.setColor(i, j, Color.BLACK);
                    }else {
                        lightPW.setColor(i, j, new Color(0,0,0,0));
                        darkPW.setColor(i, j, new Color(0,0,0,0));
                    }
                }
            }

            // write the two images into files
            try {
                String path = "src/CustomSprites/";

                // write to file
                ImageIO.write(SwingFXUtils.fromFXImage(lightVariant, null), "png", new File(path + "Light" + pieceName.getText() + ".png"));
                ImageIO.write(SwingFXUtils.fromFXImage(darkVariant, null), "png", new File(path + "Dark" + pieceName.getText() + ".png"));
                 
                System.out.println("Saved Finished!");
            }catch (IOException e) {
                System.out.println("Error in saving image!");
            }

        });

        // button to add a new move rule to the piece
        Button addMoveRule = new Button("Add Move Rule");

        imageManager.getChildren().addAll(title, pieceName, canvas, save);
        root.setLeft(skeleton);

        scene.setRoot(root);
    }
}

// this class is used to start a new game once one has finished
// this is for when a chess game is actually running
class RepeatGameThread extends Thread {
    private Entity player1;
    private Entity player2;
    private ChessGame cg;
    private MenuManager mm;

    public RepeatGameThread(Entity player1, Entity player2, ChessGame cg, MenuManager mm) {
        this.player1 = player1;
        this.player2 = player2;
        this.cg = cg;
        this.mm = mm;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            // repeat thread until endCon != -1 by creating a new thread every check
            if(cg.getEndCon() == -1) {
                RepeatGameThread rgt = new RepeatGameThread(player1, player2, cg, mm);
                rgt.start();
                return;
            }

            if(cg.endThreadsFlag) {
                return;
            }

            // now game has ended
            int winner = cg.getWinner();
            if(winner != -1) mm.incrementWins(winner - 1);
            mm.updateWinsText();

            // begin new game
            cg.createNewChessGame(player1, player2);

            // repeat thread
            RepeatGameThread rgt = new RepeatGameThread(player1, player2, cg, mm);
            rgt.start();
        });
    }
}

// this class manages the "move rule" system
class MoveRule {

}

// enums to represent the various prefix/suffixes a move can have
// Capture availability notes how the piece interacts with the opponent's pieces
enum Capture_Availability {
    NONE, ABLE, ONLY;

    public String stringRep() {
        switch(this) {
            case NONE:
                return "";
            case ABLE:
                return "C";
            case ONLY:
                return "O";
        }
        return "error";
    }
}
// denotes the various kinds of looping (SINGLE = no looping) (STARTING SEQUENCE = only 1 iteration unless the piece hasnt moved)
enum Looping_Type {
    SINGLE, CONTINUOUS, SEQUENCE, STARTING_SEQUENCE, STARTING_MOVE_ONLY;

    public String stringRep() {
        switch(this) {
            case SINGLE:
                return "";
            case CONTINUOUS:
                return "L";
            case SEQUENCE:
                return "I";
            case STARTING_SEQUENCE:
                return "SI";
            case STARTING_MOVE_ONLY:
                return "S";
        }
        return "error";
    }
}
// stores the four directions a piece can move in
enum Direction {
    NORTH, SOUTH, EAST, WEST;

    public String stringRep() {
        switch(this) {
            case NORTH:
                return "N";
            case SOUTH:
                return "S";
            case EAST:
                return "E";
            case WEST:
                return "W";
        }
        return "error";
    }
} 