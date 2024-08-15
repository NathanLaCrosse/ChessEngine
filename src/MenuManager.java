// this class manages the main GUI of athe chess program, creating the various menus and screens

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

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

    ///////////////////////////////////////////// MAIN MENU
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

        Button boardCreator = new Button("Create New Board");
        boardCreator.setOnAction(event -> {
            buildBoardCreationScreen();
        });

        skeleton.getChildren().addAll(newGame, pieceCreator, boardCreator);
        root.setCenter(skeleton);

        scene.setRoot(root);
    }

    ///////////////////////////////////////////// GAME CREATION CONFIG
    private static HashMap<String, String[][]> boardLookup = null;
    private static HashMap<String, ChessPiece> customPieceLookup = null;
    private static HashMap<String, Pair<WritableImage, WritableImage>> imageLookup = null;
    private static ComboBox<String> selectedBoardName = null;
    private void buildGameSelectionScreen() {
        BorderPane root = new BorderPane();

        HBox skeleton = new HBox();
        skeleton.setSpacing(30);
        VBox playerSelect = new VBox();
        VBox customSettings = new VBox();
        skeleton.getChildren().addAll(playerSelect, customSettings);

        /////////////// HOMEBREW SELECTION
        CheckBox enableCustomPieces = new CheckBox("Customized Content Enabled");
        customSettings.getChildren().add(enableCustomPieces);
        enableCustomPieces.setOnMouseClicked(event -> {

            // if this is being unchecked, remove any boxes from customsettings that were added
            if(!enableCustomPieces.isSelected()) {
                while(customSettings.getChildren().size() > 1) {
                    customSettings.getChildren().remove(1);
                }
                return;
            }

            // all code past this point require that the button has just been checked

            boardLookup = new HashMap<>();
            boardLookup.put("Default",Board.DEFAULT_BOARD_REP);
            createCustomBoardLookupTable(boardLookup);
            
            // add all board names from the lookup to a dropdown to select a board
            HBox boardContain = new HBox();
            Label boardLabel = new Label("Select a loaded board: ");
            selectedBoardName = new ComboBox<>();
            selectedBoardName.getItems().addAll(boardLookup.keySet());
            boardContain.getChildren().addAll(boardLabel, selectedBoardName);

            // load up pieces for future reference
            customPieceLookup = new HashMap<>();
            imageLookup = new HashMap<>();
            createCustomPieceLookupTables(customPieceLookup, imageLookup);

            customSettings.getChildren().addAll(boardContain);
        });

        /////////////// PLAYER SELECT
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
        root.setCenter(skeleton);

        Button startGame = new Button("Start Game");
        startGame.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event arg0) {
                Entity player1 = createEntityBasedOnString(whitePlayerSelected.getSelectionModel().getSelectedItem(), true);
                Entity player2 = createEntityBasedOnString(blackPlayerSelected.getSelectionModel().getSelectedItem(), false);

                if(!enableCustomPieces.isSelected()) {
                    createChessGameView(player1, player2, Board.DEFAULT_BOARD_REP, null, null);
                }else {
                    createChessGameView(player1, player2, boardLookup.get(selectedBoardName.getValue()), customPieceLookup, imageLookup);
                }
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
    // create a data structure that allows for custom pieces to be looked up based on their name
    // this is passed onto a board interpreting process so that custom boards can be loaded
    // also imports the sprites :)
    private void createCustomPieceLookupTables(HashMap<String, ChessPiece> pieceLookup, HashMap<String, Pair<WritableImage, WritableImage>> customSprites) {
        try {
            Scanner fileIn = new Scanner(new File("src/CustomData/CustomPieceData.txt"));

            while (fileIn.hasNextLine()) {
                String nameText = fileIn.nextLine().substring(12).trim();         
                int material = Integer.parseInt(fileIn.nextLine().substring(9).trim());
                
                boolean[][] boolBoard = new boolean[64][64]; // for handling the sprite
                for(int i = 0; i < 64; i++) {
                    String dissect = fileIn.nextLine().substring(1);
                    for(int k = 0; k < 64; k++) {
                        boolBoard[i][k] = dissect.charAt(k) == '1';
                    }
                }
                // convert boolboard into light/dark variants of the piece
                // done twice for white/black variants
                if(customSprites != null) {
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
                    Pair<WritableImage, WritableImage> pieceImgs = new Pair<>(lightVariant, darkVariant);
                    customSprites.put(nameText, pieceImgs);
                }

                int numMoves = Integer.parseInt(fileIn.nextLine().substring(7, 8));
                String[] moveInstructions = new String[numMoves];
                for(int i = 0; i < moveInstructions.length; i++) {
                    moveInstructions[i] = fileIn.nextLine().trim();
                }

                ChessPiece examplePiece = new ChessPiece(true, nameText, material, moveInstructions);
                pieceLookup.put(nameText, examplePiece);
            }
            fileIn.close();
        }catch (IOException e) {
            System.out.println("Reading piece data failed!");
        }
    }
    // loads up all of the boards saved in the customboarddata.txt file and puts them into a hashmap for later lookup
    private void createCustomBoardLookupTable(HashMap<String, String[][]> boardLookup) { 
        try {
            Scanner fileIn = new Scanner(new File("src/CustomData/CustomBoardData.txt"));

            while (fileIn.hasNextLine()) {
                String boardName = fileIn.nextLine().substring(13);
                String[][] layout = new String[8][8];

                for(int i = 0; i < 8; i++) {
                    for(int k = 0; k < 8; k++) {
                        layout[i][k] = fileIn.next();
                    }
                    fileIn.nextLine();
                }

                boardLookup.put(boardName, layout);
            }

            fileIn.close();
        }catch (IOException e) {
            System.out.println("Reding piece data failed!");
        }
    }

    ///////////////////////////////////////////// GAME SCREEN
    private Label p1wins;
    private Label p2wins;
    private void createChessGameView(Entity player1, Entity player2, String[][] boardStr, HashMap<String, ChessPiece> customPieces, HashMap<String, Pair<WritableImage, WritableImage>> customPieceImages) {
        wins = new int[]{0,0};
        BorderPane root = new BorderPane();
        ChessGame cg = new ChessGame();

        HBox layout = new HBox();
        RepeatGameThread rgt = null;

        // set up chess game - this is more complicated if homebrew pieces/maps are involved
        if(customPieces == null) {
            cg.createNewChessGame(player1, player2, Board.DEFAULT_BOARD_REP, null, null);
            rgt = new RepeatGameThread(player1, player2, cg, this, Board.DEFAULT_BOARD_REP, null, null);
        }else {
            HashMap<String, ChessPiece> completePieceLookup = Board.cloneVanillaPieceLookup();
            for(Entry<String,ChessPiece> pair : customPieces.entrySet()) {
                completePieceLookup.put(pair.getKey(), pair.getValue());
            }

            cg.createNewChessGame(player1, player2, boardStr, completePieceLookup, imageLookup);
            rgt = new RepeatGameThread(player1, player2, cg, this, boardStr, completePieceLookup, customPieceImages);
        }
        layout.getChildren().add(cg.getGuiComponent());
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

    ///////////////////////////////////////////// PIECE CREATOR
    private void buildPieceCreatorScreen() {
        BorderPane root = new BorderPane();

        HBox skeleton = new HBox();
        VBox imageManager = new VBox();
        VBox moveAdditions = new VBox();
        skeleton.getChildren().addAll(imageManager, moveAdditions);

        Button toMainMenu = new Button("Main Menu");
        toMainMenu.setOnAction(event -> {
            buildMainMenu();
        });
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
        TextField material = new TextField();
        material.setPromptText("Enter Material");

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

        // button to save entire piece into memory
        Button save = new Button("Save Piece");
        save.setOnMouseClicked(event -> {
            String[] moveInstructions = new String[moveRules.size()];
            Iterator<MoveRule> itr = moveRules.iterator();
            int i = 0;
            while(itr.hasNext()) {
                MoveRule r = itr.next();
                moveInstructions[i] = r.convertToMoveInstruction();
                i++;
            }

            writePieceDataToFile(pieceName.getText(), Integer.parseInt(material.getText()), boolBoard, moveInstructions);
        });

        moveCreator.getChildren().addAll(buttons, innerSkeleton);

        imageManager.getChildren().addAll(title, pieceName, material, canvas, preview, save, toMainMenu);
        root.setLeft(skeleton);
        skeleton.getChildren().add(moveCreator);

        scene.setRoot(root);
    }
    // helper method to make it easier to understand how a piece's data in converted to the data file
    private void writePieceDataToFile(String pieceName, int material, boolean[][] imgBoolBoard, String[] moveInstuctions) {
        String dataText = "Piece Name - " + pieceName + "\nMaterial: " + material;
        for(int i = 0; i < 64; i++) {
            dataText += "\n[";
            for(int j = 0; j < 64; j++) {
                dataText += imgBoolBoard[i][j] ? 1 : 0;
            }
            dataText += "]";
        }
        dataText += "\nMoves ("+moveInstuctions.length+"):";
        for(int i = 0; i < moveInstuctions.length; i++) {
            dataText += "\n" + moveInstuctions[i];
        }

        try (FileWriter fw = new FileWriter("src/CustomData/CustomPieceData.txt", true) ; BufferedWriter wr = new BufferedWriter(fw)) {
            wr.write(dataText);
            wr.close();
        }catch (IOException e) {
            System.out.println("Error in writing piece data to file!");
        }
    }

    ///////////////////////////////////////////// BOARD CREATOR
    private static final String[] ORIGINAL_PIECES = {"King","Queen","Bishop","Knight","Rook","Pawn"};
    private void buildBoardCreationScreen() {
        BorderPane root = new BorderPane();
        VBox skeleton = new VBox();
        root.setLeft(skeleton);

        Label title = new Label("Board Creator\nPlease note that the top half will be considered as black's pieces and the bottom will be white's.");

        ArrayList<String> validPieceNames = new ArrayList<>();
        validPieceNames.addAll(Arrays.asList(ORIGINAL_PIECES));

        // load in the custom pieces and add them to the allowed names
        HashMap<String, ChessPiece> pieceLookup = new HashMap<>();
        createCustomPieceLookupTables(pieceLookup, null);
        validPieceNames.addAll(pieceLookup.keySet());

        String pieceListText = "\nValid Piece Names:\n";
        for(String name : validPieceNames) {
            pieceListText += "\n" + name;
        }
        Label allowedPieces = new Label(pieceListText + "\n");

        TextField[][] positions = new TextField[8][8];
        VBox fieldContainer = new VBox();

        for(int i = 0; i < 8; i++) {
            HBox row = new HBox();

            for(int k = 0; k < 8; k++) {
                TextField posField = new TextField();
                posField.setPromptText("Enter Name");
                posField.setPrefSize(100, 30);
                positions[i][k] = posField;
                row.getChildren().add(posField);
            }

            fieldContainer.getChildren().add(row);
        }

        HBox contain = new HBox();
        Label namePrompt = new Label("Enter Board Name: ");
        TextField boardName = new TextField();
        contain.getChildren().addAll(namePrompt, boardName);

        HBox saveContain = new HBox();
        Button save = new Button("Save Board");
        Label errorText = new Label();
        save.setOnAction(event -> {
            if(boardName.getText().isBlank() || allTextFieldsEmpty(positions)) {
                errorText.setText("Cannot Create Blank Board!");
                return;
            }

            // validate each name to make sure it corresponds to a saved piece
            for(TextField[] tfs : positions) {
                for(TextField tf : tfs) {
                    // exit if unvalid names
                    if(!validPieceNames.contains(tf.getText()) && !tf.getText().isBlank()) {
                        errorText.setText("Invalid Name(s) Detected!");
                        return;
                    }
                    if(tf.getText().equals("Blank")) {
                        errorText.setText("Blank is a prohibited name!");
                        return;
                    }
                }
            }

            // write all of the names in the textfields into the file
            writeBoardToFile(boardName, positions);

            errorText.setText("Saved Successfully!");
        });
        saveContain.getChildren().addAll(save, errorText);

        Button mainMenu = new Button("Main Menu");
        mainMenu.setOnAction(event -> {
            buildMainMenu();
        });

        skeleton.getChildren().addAll(title, allowedPieces, fieldContainer, contain, saveContain, mainMenu);

        scene.setRoot(root);
    }
    private boolean allTextFieldsEmpty(TextField[][] tfs) {
        for(int i = 0; i < tfs.length; i++) {
            for(int k = 0; k < tfs[0].length; k++) {
                if(!tfs[i][k].getText().isBlank()) return false;
            }
        }
        return true;
    }
    private void writeBoardToFile(TextField boardName, TextField[][] tfs) {
        String dataText = "Board Name - " + boardName.getText();
        
        for(int i = 0; i < tfs.length; i++) {
            dataText += "\n";
            for(int k = 0; k < tfs[0].length; k++) {
                if(tfs[i][k].getText().isEmpty()) {
                    dataText += "Blank ";
                }else {
                    dataText += tfs[i][k].getText() + " ";
                }
            }
        }

        try (FileWriter fw = new FileWriter("src/CustomData/CustomBoardData.txt", true) ; BufferedWriter wr = new BufferedWriter(fw)) {
            wr.write(dataText);
            wr.close();
        }catch (IOException e) {
            System.out.println("Error in writing piece data to file!");
        }
    }
}

/**
 * Some old code for saving images
 * // use the values from the boolboard to write the correct pixels colors on two writable images
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
                String path = "src/CustomData/";

                // write to file
                ImageIO.write(SwingFXUtils.fromFXImage(lightVariant, null), "png", new File(path + "Light" + pieceName.getText() + ".png"));
                ImageIO.write(SwingFXUtils.fromFXImage(darkVariant, null), "png", new File(path + "Dark" + pieceName.getText() + ".png"));
                 
                System.out.println("Saved Finished!");
            }catch (IOException e) {
                System.out.println("Error in saving image!");
            }
*/

// this class is used to start a new game once one has finished
// this is for when a chess game is actually running
class RepeatGameThread extends Thread {
    private Entity player1;
    private Entity player2;

    private String[][] boardLayout;
    private HashMap<String, ChessPiece> pieceLookup;
    private HashMap<String, Pair<WritableImage, WritableImage>> imageLookup;

    private ChessGame cg;
    private MenuManager mm;

    public RepeatGameThread(Entity player1, Entity player2, ChessGame cg, MenuManager mm, String[][] boardLayout, HashMap<String, ChessPiece> pieceLookup, HashMap<String, Pair<WritableImage, WritableImage>> imageLookup) {
        this.player1 = player1;
        this.player2 = player2;
        this.cg = cg;
        this.mm = mm;

        this.boardLayout = boardLayout;
        this.pieceLookup = pieceLookup;
        this.imageLookup = imageLookup;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            // repeat thread until endCon != -1 by creating a new thread every check
            if(cg.getEndCon() == -1) {
                RepeatGameThread rgt = new RepeatGameThread(player1, player2, cg, mm, boardLayout, pieceLookup, imageLookup);
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
            cg.createNewChessGame(player1, player2, boardLayout, pieceLookup, imageLookup);

            // repeat thread
            RepeatGameThread rgt = new RepeatGameThread(player1, player2, cg, mm, boardLayout, pieceLookup, imageLookup);
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