import java.util.LinkedList;

import javafx.application.Platform;
import javafx.util.Pair;

public class Player extends Entity {
    private ChessPiece selectedPiece = null;
    private Pair<Integer, Integer> selectedPiecePos = null;
    private Move selectedMove = null;

    public Player(boolean side) {
        super(side);
    }

    @Override
    public Move selectMove(Board b) {
        // wait until move has been selected (sleep thread we are on)
        while(selectedMove == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return selectedMove; 
    }

    protected Move selectedMove() {
        return selectedMove;
    }

    // selects a given move to play if the selected move is a valid move
    // of the previously selected piece
    public void selectIfCanPlay(LinkedList<Move> validMoves, Pair<Integer, Integer> dest) {
        if(selectedPiece == null) return;

        // look at valid moves and see if one fits the move we're trying to make
        for(Move m : validMoves) {
            if(m.getStart().equals(selectedPiecePos) && m.getDest().equals(dest)) {
                selectedMove = m;
                return;
            }
        }
    }

    public void setPiece(ChessPiece piece, Pair<Integer, Integer> piecePos) {
        this.selectedPiece = piece;
        this.selectedPiecePos = piecePos;
    }
    public ChessPiece getSelectedPiece() {
        return selectedPiece;
    }

    @Override
    public void reset() {
        this.selectedMove = null;
        this.selectedPiece = null;
        this.selectedPiecePos = null;
    }

    public static void createPlayerPickingThread(ChessGame cg, Player player) {
        MovePickingThread mpt = new MovePickingThread(cg, player);
        mpt.start();
    }
}

// allows for move picking to work simultaneously with the javafx program
class MovePickingThread extends Thread {
    private Player player;
    private ChessGame cg;

    public MovePickingThread(ChessGame cg, Player player) {
        this.player = player;
        this.cg = cg;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            if(player.selectedMove() != null) {
                cg.endPlayerTurn(player.selectedMove());
            }else {
                MovePickingThread mpt = new MovePickingThread(cg, player);
                mpt.start();
            }
        });
    }
}
