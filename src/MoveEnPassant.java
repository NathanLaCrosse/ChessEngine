import javafx.util.Pair;

public class MoveEnPassant extends Move {
    private Pair<Integer, Integer> capturedPawnPos;
    private int capturedDex;

    public MoveEnPassant(Board b, Pair<Integer, Integer> start, Pair<Integer, Integer> dest, Pair<Integer, Integer> capturedPawnPos) {
        super(b, start, dest);
        this.capturedPawnPos = capturedPawnPos;
        this.capturedDex = b.getIndexMap()[capturedPawnPos.getKey()][capturedPawnPos.getValue()];
    }
    
    @Override
    public void move() {
        ChessPiece moving = b.pieceAt(start);
        if(!moving.hasMoved()) {
            freshlyMoved = true;
            moving.setIfMoved(true);
        }

        b.setPieceLocation(start, -1);
        b.setPieceLocation(capturedPawnPos, -1);
        b.setPieceLocation(dest, startDex);

        b.fiftyMoveCounter = 0;
    }

    @Override 
    public void undoMove() {
        b.setPieceLocation(start, startDex);
        b.setPieceLocation(capturedPawnPos, capturedDex);
        b.setPieceLocation(dest, endDex);

        if(freshlyMoved) {
            b.pieceAt(start).setIfMoved(false);
        }

        b.fiftyMoveCounter = previousFiftyMoveCounter;
    }

    // this get method does not create an alias so capturedPawnPos cannot be changed
    public Pair<Integer, Integer> getCapturedPawnPos() {
        return new Pair<>(capturedPawnPos.getKey(), capturedPawnPos.getValue());
    }
}
