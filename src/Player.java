import java.util.LinkedList;

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
}
