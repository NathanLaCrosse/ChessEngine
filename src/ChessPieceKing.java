import java.util.LinkedList;

import javafx.util.Pair;

public class ChessPieceKing extends ChessPiece {

    public ChessPieceKing(boolean side, int material) {
        super(side, "King", material, ChessPiece.DEFAULT_KING_MOVES);
    }
    
    // extra functionality provided for the castle move
    @SuppressWarnings("unchecked")
    @Override
    public LinkedList<Move> generateMoves(Board b, Pair<Integer, Integer> location) {
        LinkedList<Move> moves = super.generateMoves(b, location);

        if(hasMoved()) return moves; // can't castle if already moved

        Object[] possiblePosi = new Object[]{new Pair<>(location.getKey(), 0),new Pair<>(location.getKey(), b.getXDim()-1)};
        
        // check each possible rook position for a rook and then see if the conditions are right for a castle
        for(int i = 0; i < possiblePosi.length; i++) {
            Pair<Integer, Integer> possibleRookPos = (Pair<Integer, Integer>)possiblePosi[i];
            ChessPiece possibleRook = b.pieceAt(possibleRookPos);

            int direction = i == 0 ? -1 : 1;
            if(possibleRook != null && possibleRook.getName().equals("Rook") && possibleRook.getSide() == getSide() && !possibleRook.hasMoved()
                && clearLineOfSight(b, location.getKey(), location.getValue() + direction, possibleRookPos.getValue(), direction)) {
                
                Pair<Integer, Integer> kingDest = new Pair<>(location.getKey(), location.getValue() + (2*direction));
                Pair<Integer, Integer> rookDest = new Pair<>(location.getKey(), location.getValue() + direction);

                if(!b.inBounds(kingDest)) continue; // skip over this possible rook if move is out of bounds

                moves.add(new MoveCastle(b, location, kingDest, possibleRookPos, rookDest));
            }
        }

        return moves;
    } 

    private boolean clearLineOfSight(Board b, int row, int col, int endBeforeCol, int direction) {
        if(col == endBeforeCol) return true; // successfully traversed to end

        return b.getIndexMap()[row][col] == -1 && clearLineOfSight(b, row, col + direction, endBeforeCol, direction);
    }
}
