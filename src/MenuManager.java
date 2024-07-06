// this class manages the main GUI of athe chess program, creating the various menus and screens

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

        Button newGame = new Button("Create New Game");
        newGame.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event arg0) {
                buildGameSelectionScreen();
            }
            
        });
        root.setCenter(newGame);

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
}

// this class is used to start a new game once one has finished
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
