// this class handles both the graphics of the chess game and
// the turn-based structure of it


import java.util.LinkedList;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import javafx.event.EventHandler;

public class ChessGame {
    private VBox guiComponent;

    private Board gameBoard;
    private boolean turn;

    private Entity player1;
    private Entity player2;
    private boolean playerTrigger = false;

    private BoardTile[][] tiles;

    // used by the board tiles to figure out where a player can move
    private LinkedList<Move> validMoves = null;

    public ChessGame() {
        guiComponent = new VBox();
        //guiComponent.setSpacing(64);
        gameBoard = null;
        turn = true;
        player1 = null;
        player2 = null;

        tiles = new BoardTile[8][8];

        // TODO: Update this to any supported board size
        for(int i = 0; i < 8; i++) {
            HBox rank = new HBox();
            for(int k = 0; k < 8; k++) {
                // for alternating black / white color
                boolean tileColor = (i + k) % 2 == 0;

                Pair<Integer, Integer> pos = new Pair<Integer, Integer>(i,k);
                BoardTile tile = new BoardTile(this, tileColor, pos);
                tiles[i][k] = tile;

                rank.getChildren().add(tile.getImageContainer());
            }
            guiComponent.getChildren().add(rank);
        }

        updateSprites();
    }

    protected void createNewChessGame(Entity player1, Entity player2) {
        gameBoard = new Board();
        this.player1 = player1;
        this.player2 = player2;

        updateSprites();

        // create and run the game's thread process
        GameThread t = new GameThread(this);
        t.start();
    }

    // returns true if the game has ended
    protected boolean nextTurn() {
        // TODO: check for end of game first
        validMoves = gameBoard.generateMoves(turn);

        Move moveToPlay = currentPlayer().selectMove(gameBoard);

        if(!validMoves.contains(moveToPlay)) {
            System.out.println("Illegal Move!!!");
        }

        moveToPlay.move();
        currentPlayer().reset();

        turn = !turn;
        return false;
    }

    protected void triggerPlayerTurn() {
        // TODO: check for end of game first

        if(!playerTrigger) {
            flagPlayerTrigger();
            validMoves = gameBoard.generateMoves(turn);
            Player.createPlayerPickingThread(this, (Player)currentPlayer());
        }
    }
    protected void endPlayerTurn(Move m) {
        if(!validMoves.contains(m)) {
            System.out.println("Illegal Move!!!");
        }

        m.move();
        currentPlayer().reset();
        clearPlayerTrigger();
        updateSprites();

        turn = !turn;
    }

    public VBox getGuiComponent() {
        return guiComponent;
    }

    protected Board getGameBoard() {
        return gameBoard;
    }
    protected Entity currentPlayer() {
        if(gameBoard == null) return null;
        if(turn) {
            return player1;
        }else {
            return player2;
        }
    }
    protected LinkedList<Move> getValidMoves() {
        return validMoves;
    }

    // makes sure a player's turn is only started once
    protected void flagPlayerTrigger() {
        playerTrigger = true;
    }
    protected void clearPlayerTrigger() {
        playerTrigger = false;
    }

    // change the graphics of each tile if pieces have been moved    
    public void updateSprites() {
        for(BoardTile[] bts : tiles) {
            for(BoardTile bt : bts) {
                bt.determinePieceSprite();
                bt.disableMovementDot();
            }
        }
    }

    public void displayMovementDotsForPieceAtPos(Pair<Integer, Integer> pos) {
        if(gameBoard == null) return;

        // check every vaid move and see if it can be made by the given piece
        for(Move m : validMoves) {
            if(m.getStart().equals(pos)) {
                tiles[m.getDest().getKey()][m.getDest().getValue()].enableMovementDot();
            }
        }

    }
}

// this thread runs the game on the javafx thread in order to not have conflict with it
class GameThread extends Thread {
    private ChessGame cg;

    public GameThread(ChessGame cg) {
        this.cg = cg;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            if(!(cg.currentPlayer() instanceof Player)) {
                boolean keepGoing = cg.nextTurn();
                cg.updateSprites();
                if(!keepGoing) {
                    GameThread t2 = new GameThread(cg);
                    t2.start();
                }
            }else {
                cg.triggerPlayerTurn();
                GameThread t2 = new GameThread(cg);
                t2.start();
            }
        });
    }
}

// bundles together the different layers used to represent tiles and pieces in the game
class BoardTile {
    private ChessGame game;

    private VBox imageContainer;
    private ImageView tileDraw;
    private ImageView pieceDraw; // on top of tile
    private ImageView moveDot; // visual to show that a piece could move here

    private Pair<Integer, Integer> posOnBoard;

    public BoardTile(ChessGame game, boolean tileColor, Pair<Integer, Integer> posOnBoard) {
        this.game = game;
        this.posOnBoard = posOnBoard;

        imageContainer = new VBox();
        imageContainer.setSpacing(-64); // negative spacing so the piece is on top of the tile (64 pixels for tile sprite)

        tileDraw = new ImageView();
        pieceDraw = new ImageView();
        moveDot = new ImageView();

        imageContainer.getChildren().addAll(tileDraw, pieceDraw, moveDot);

        if(tileColor) {
            tileDraw.setImage(new Image(this.getClass().getResourceAsStream("Sprites/LightSquare.png")));
        }else {
            tileDraw.setImage(new Image(this.getClass().getResourceAsStream("Sprites/DarkSquare.png")));
        }

        determinePieceSprite();

        // adds mouse functionality to the board
        imageContainer.setOnMousePressed(new EventHandler<Event>() {

            @Override
            public void handle(Event arg0) {
                if(game.getGameBoard() == null) return;

                Board b = game.getGameBoard();
                ChessPiece piece = b.pieceAt(posOnBoard);

                if(game.currentPlayer() instanceof Player) {
                    Player p = (Player)game.currentPlayer();
                    
                    // if(piece == null || piece.getSide() != p.getSide() // select a move
                    //         || /*Exception for castles*/(selectedKing != null && Math.abs(selectedKing.currentPos.second() - positionOnBoard.second()) == 2)) {
                    //     p.selectIfCanPlay(positionOnBoard);
                    //     game.updateSprites();
                    // }else 
                    
                    if(piece == null || piece.getSide() != p.getSide()) {
                        p.selectIfCanPlay(game.getValidMoves(), posOnBoard);
                        game.updateSprites();
                    }else if(piece != null && piece.getSide() == p.getSide()) { // select a piece 
                        p.setPiece(piece, posOnBoard);
                        game.updateSprites();
                        game.displayMovementDotsForPieceAtPos(posOnBoard);
                    }
                }
            }
            
        });
    }

    // displays a movement dot to show a certain piece could move here
    // input is the piece that is enabling the dots
    public void enableMovementDot() {
        moveDot.setImage(new Image(this.getClass().getResourceAsStream("Sprites/SelectionDot.png")));
        //if(piece instanceof King) selectedKing = piece;
    }
    // stops displaying the movement dot
    public void disableMovementDot() {
        moveDot.setImage(new Image(this.getClass().getResourceAsStream("Sprites/Blank.png")));
        //selectedKing = null;
    }

    public void determinePieceSprite() {
        if(game.getGameBoard() == null) {
            pieceDraw.setImage(new Image(this.getClass().getResourceAsStream("Sprites/Blank.png")));
            return;
        }

        ChessPiece pieceOnTile = game.getGameBoard().pieceAt(posOnBoard);

        String nameOfSprite = "Blank";
        if(pieceOnTile != null) {
            nameOfSprite = (pieceOnTile.getSide() ? "Light" : "Dark") + pieceOnTile.getName(); // assumes the piece name lines up with the file names (rook should be "Rook")
        }

        Image img = new Image(this.getClass().getResourceAsStream("Sprites/"+nameOfSprite+".png"));

        pieceDraw.setImage(img);
    }

    public VBox getImageContainer() {
        return imageContainer;
    }
}
