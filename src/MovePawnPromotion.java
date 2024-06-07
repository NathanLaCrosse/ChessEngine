import javafx.util.Pair;

public class MovePawnPromotion extends Move {
    private String newPieceName;
    private int newMaterial;
    private String[] newMoveInstructions;

    public MovePawnPromotion(Board b, Pair<Integer, Integer> start, Pair<Integer, Integer> dest, String newPieceName, int newMaterial, String[] newMoveInstructions) {
        super(b, start, dest);
        this.newPieceName = newPieceName;
        this.newMaterial = newMaterial;
        this.newMoveInstructions = newMoveInstructions;
    }
    
    @Override
    public void move() {
        // convert the moving pawn into the new piece
        ChessPiece movingPiece = b.pieceAt(start);
        movingPiece.setName(newPieceName);
        movingPiece.setMaterial(newMaterial);
        movingPiece.setMoveInstructions(newMoveInstructions);

        // perform traditional move
        super.move();
    }

    @Override 
    public void undoMove() {
        // undo traditional move
        super.undoMove();

        // convert back to pawn
        ChessPiece movingPiece = b.pieceAt(start);
        movingPiece.setName("Pawn");
        movingPiece.setMaterial(1);
        movingPiece.setMoveInstructions(ChessPiece.DEFAULT_PAWN_MOVES);
    }
    
}
