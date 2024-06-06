import javafx.util.Pair;

public class MoveCastle extends Move {
    private Pair<Integer, Integer> rookStart;
    private Pair<Integer, Integer> rookDest;
    private int rookStartDex;
    private int rookEndDex;

    public MoveCastle(Board b, Pair<Integer, Integer> start, Pair<Integer, Integer> dest, Pair<Integer, Integer> rookStart, Pair<Integer, Integer> rookDest) {
        super(b, start, dest);
        this.rookStart = rookStart;
        this.rookDest = rookDest;

        this.rookStartDex = b.getIndexMap()[rookStart.getKey()][rookStart.getValue()];
        this.rookEndDex = b.getIndexMap()[rookDest.getKey()][rookDest.getValue()];
    }

    @Override
    public void move() {
        ChessPiece moving = b.pieceAt(start);
        moving.setIfMoved(true);
        b.updateKingPosition(moving.getSide(), dest);

        ChessPiece rook = b.pieceAt(rookStart);
        rook.setIfMoved(true);

        b.setPieceLocation(start, -1);
        b.setPieceLocation(dest, startDex);
        b.setPieceLocation(rookStart, -1);
        b.setPieceLocation(rookDest, rookStartDex);
    }

    @Override
    public void undoMove() {
        b.setPieceLocation(start, startDex);
        b.setPieceLocation(dest, endDex);
        b.setPieceLocation(rookStart, rookStartDex);
        b.setPieceLocation(rookDest, rookEndDex);

        ChessPiece moving = b.pieceAt(start);
        moving.setIfMoved(false);
        b.updateKingPosition(moving.getSide(), start);

        ChessPiece rook = b.pieceAt(rookStart);
        rook.setIfMoved(false);
    }

    // designed in a way that avoids aliases, so that the values of these variables cannot be changed
    public Pair<Integer, Integer> getRookStart() {
        return new Pair<>(rookStart.getKey(), rookStart.getValue());
    }
    public Pair<Integer, Integer> getRookDest() {
        return new Pair<>(rookDest.getKey(), rookDest.getValue());
    }

}