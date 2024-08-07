// this class manages the main GUI of athe chess program, creating the various menus and screens

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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

        // buttons to add/remove moves from the custom piece
        VBox moveCreator = new VBox();
        HBox buttons = new HBox();

        Button addMoveRule = new Button("Add Move Rule");
        LinkedList<MoveRule> moveRules = new LinkedList<>();
        VBox innerSkeleton = new VBox();
        innerSkeleton.setSpacing(20);
        addMoveRule.setOnMouseClicked(event -> {
            MoveRule mr = new MoveRule(moveRules.size());
            moveRules.add(mr);
            innerSkeleton.getChildren().add(mr.getSkeleton());
        });

        Button removeMove = new Button("Remove Move Rule");
        removeMove.setOnMouseClicked(event -> {
            if(moveRules.size() <= 0) return;

            innerSkeleton.getChildren().remove(moveRules.getLast().getSkeleton());
            moveRules.removeLast();
        });

        buttons.getChildren().addAll(addMoveRule, removeMove);

        // button to preview properties of the piece
        Button preview = new Button("Preview");
        preview.setOnMouseClicked(event -> {
           for(MoveRule mr : moveRules) {
                System.out.println(mr.convertToMoveInstruction());
           }
        });

        moveCreator.getChildren().addAll(buttons, innerSkeleton);

        imageManager.getChildren().addAll(title, pieceName, canvas, preview, save);
        root.setLeft(skeleton);
        skeleton.getChildren().add(moveCreator);

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

// this class manages the "move rule" system so that a user can create new moves
class MoveRule {
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

        public boolean isSequence() {
            return this == SEQUENCE || this == STARTING_SEQUENCE;
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

    private VBox skeleton;
    private Capture_Availability capAv;
    private Looping_Type loopType;
    private TextField sequenceLen, directionText;

    public MoveRule(int num) {
        capAv = Capture_Availability.NONE;
        loopType = Looping_Type.SINGLE;
        sequenceLen = null;

        skeleton = new VBox();
        skeleton.setSpacing(5);
        
        Label title = new Label("Move " + (num + 1));
        skeleton.getChildren().add(title);

        buildCaptureAvailability();
        buildLoopingTypes();

        Label directionLabel = new Label("Enter Directions With a Chain of Cardinal Directions (ex: NNE)");
        directionText = new TextField();
        directionText.setPromptText("Enter Directions");
        HBox dirBox = new HBox();
        dirBox.setSpacing(5);
        dirBox.getChildren().addAll(directionLabel, directionText);

        skeleton.getChildren().add(dirBox);
    }

    private void buildCaptureAvailability() {
        HBox row = new HBox();
        row.setSpacing(5);

        ToggleGroup tg = new ToggleGroup();

        RadioButton r1 = new RadioButton("Cannot Capture");
        r1.setOnMouseClicked(event -> {
            capAv = Capture_Availability.NONE;
        });
        RadioButton r2 = new RadioButton("Can Capture");
        r2.setOnMouseClicked(event -> {
            capAv = Capture_Availability.ABLE;
        });
        RadioButton r3 = new RadioButton("Can Only Capture");
        r3.setOnMouseClicked(event -> {
            capAv = Capture_Availability.ONLY;
        });

        r1.setToggleGroup(tg);
        r2.setToggleGroup(tg);
        r3.setToggleGroup(tg);

        row.getChildren().addAll(r1, r2, r3);
        skeleton.getChildren().add(row);
    }

    private void buildLoopingTypes() {
        HBox row = new HBox();
        row.setSpacing(5);

        ToggleGroup tg = new ToggleGroup();

        RadioButton r1 = new RadioButton("No Looping");
        r1.setOnMouseClicked(event -> {
            loopType = Looping_Type.SINGLE;
        });
        RadioButton r2 = new RadioButton("Continuous Looping");
        r2.setOnMouseClicked(event -> {
            loopType = Looping_Type.CONTINUOUS;
        });
        RadioButton r3 = new RadioButton("Sequence");
        r3.setOnMouseClicked(event -> {
            loopType = Looping_Type.SEQUENCE;
        });
        RadioButton r4 = new RadioButton("Starting Sequence");
        r4.setOnMouseClicked(event -> {
            loopType = Looping_Type.STARTING_SEQUENCE;
        });
        RadioButton r5 = new RadioButton("Starting Move Only");
        r5.setOnMouseClicked(event -> {
            loopType = Looping_Type.STARTING_MOVE_ONLY;
        });

        r1.setToggleGroup(tg);
        r2.setToggleGroup(tg);
        r3.setToggleGroup(tg);
        r4.setToggleGroup(tg);
        r5.setToggleGroup(tg);

        HBox sequenceStuff = new HBox();
        Label seqLabel = new Label("Sequence Length (only for sequences): ");
        sequenceLen = new TextField();
        sequenceLen.setPromptText("Enter Length");
        sequenceStuff.getChildren().addAll(seqLabel, sequenceLen);

        row.getChildren().addAll(r1, r2, r3, r4, r5);
        skeleton.getChildren().addAll(row, sequenceStuff);
    }

    // takes this move's data and formats it for the chess program to use
    public String convertToMoveInstruction() {
        String seqLen = loopType.isSequence() ? sequenceLen.getText() : "";
        return capAv.stringRep() + loopType.stringRep() + seqLen + "|" + directionText.getText();
    }

    public VBox getSkeleton() {
        return skeleton;
    }
}