import java.util.LinkedList;

import javafx.util.Pair;

public class ChessPiecePawn extends ChessPiece {
    // used for en passant checking
    private boolean madeStartingMoveLastTurn; 

    public ChessPiecePawn(boolean side) {
        super(side, "Pawn", 1, ChessPiece.DEFAULT_PAWN_MOVES);
        madeStartingMoveLastTurn = false;
    }
    
    // extra functionality provided for the en passant move
    @SuppressWarnings("unchecked")
    @Override
    public LinkedList<Move> generateMoves(Board b, Pair<Integer, Integer> location) {
        LinkedList<Move> moves = super.generateMoves(b, location);

        if(!getName().equals("Pawn")) return moves; // this pawn is promoted - cannot perform en passant

        // positions for where a pawn captured in en passant would be
        Object[] possibleTargets = new Object[]{new Pair<>(location.getKey(),location.getValue()-1),new Pair<>(location.getKey(),location.getValue()+1)};
        
        for(int i = 0; i < possibleTargets.length; i++) {
            Pair<Integer, Integer> possiblePawnPos = (Pair<Integer,Integer>)possibleTargets[i];

            if(!b.inBounds(possiblePawnPos)) continue; // skip over out-of-bounds positions

            // if we have a pawn at a target position that made its starting move last turn, add an en passant to move list
            ChessPiece possiblePawn = b.pieceAt(possiblePawnPos);
            if(possiblePawn != null && possiblePawn.getName().equals("Pawn") && ((ChessPiecePawn)possiblePawn).madeStartingMoveLastTurn) {
                Pair<Integer, Integer> dest = new Pair<>(possiblePawnPos.getKey() + (getSide() ? -1 : 1), possiblePawnPos.getValue());

                moves.add(new MoveEnPassant(b, location, dest, possiblePawnPos));
            }
        }

        return moves;
    }

    // this method enables the en passant flag if this piece has just moved 2 spaces
    public void checkIfUsedStartingMove(Move m) {
        if(Math.abs(m.getDest().getKey() - m.getStart().getKey()) == 2) {
            madeStartingMoveLastTurn = true;
        }else {
            madeStartingMoveLastTurn = false;
        }
    }
    public void disableEnPasantFlag() {
        madeStartingMoveLastTurn = false;
    }

    // since en passant is an attacking move, it has to also be added to the attack board (unlike castling)
    // note most of code is a repeat of generating moves above
    @SuppressWarnings("unchecked")
    @Override
    public void recordMovementToAttackBoard(boolean[][] attackBoard, Board b, Pair<Integer, Integer> location) {
        super.recordMovementToAttackBoard(attackBoard, b, location);

        if(!getName().equals("Pawn")) return; // this pawn is promoted - cannot perform en passant

        // positions for where a pawn captured in en passant would be
        Object[] possibleTargets = new Object[]{new Pair<>(location.getKey(),location.getValue()-1),new Pair<>(location.getKey(),location.getValue()+1)};
        
        for(int i = 0; i < possibleTargets.length; i++) {
            Pair<Integer, Integer> possiblePawnPos = (Pair<Integer,Integer>)possibleTargets[i];

            if(!b.inBounds(possiblePawnPos)) continue; // skip over out-of-bounds positions

            // if we have a pawn at a target position that made its starting move last turn, add an en passant to move list
            ChessPiece possiblePawn = b.pieceAt(possiblePawnPos);
            if(possiblePawn != null && possiblePawn.getName().equals("Pawn") && ((ChessPiecePawn)possiblePawn).madeStartingMoveLastTurn) {
                attackBoard[possiblePawnPos.getKey()][possiblePawnPos.getValue()] = true; // record attack (on the captured pawn)
            }
        }
    }
}
