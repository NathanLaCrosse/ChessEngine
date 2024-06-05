import javafx.util.Pair;

public class Move {
    private Pair<Integer, Integer> start;
    private Pair<Integer, Integer> dest;

    private int startDex;
    private int endDex;

    private Board b;

    private boolean freshlyMoved; // stores whether or not this is the first move of the piece

    public Move(Board b, Pair<Integer,Integer> start, Pair<Integer,Integer> dest) {
        this.b = b;

        // remove the chance of aliases being created
        this.start = new Pair<Integer, Integer>(start.getKey(), start.getValue());
        this.dest = new Pair<Integer, Integer>(dest.getKey(), dest.getValue());

        this.startDex = b.getIndexMap()[start.getKey()][start.getValue()];
        this.endDex = b.getIndexMap()[dest.getKey()][dest.getValue()];
    }

    public void move() {
        ChessPiece moving = b.pieceAt(start);
        if(!moving.hasMoved()) {
            freshlyMoved = true;
            moving.setIfMoved(true);
        }
        if(moving instanceof King) {
            b.updateKingPosition(moving.getSide(), dest);
        }

        b.setPieceLocation(start, -1);
        b.setPieceLocation(dest, startDex);
    }

    public void undoMove() {
        b.setPieceLocation(start, startDex);
        b.setPieceLocation(dest, endDex);

        ChessPiece moving = b.pieceAt(start);
        if(freshlyMoved) {
            b.pieceAt(start).setIfMoved(false);
        }
        if(moving instanceof King) {
            b.updateKingPosition(moving.getSide(), start);
        }
    }

    // note that these getter do not allow aliases - purely a getter, no way to change dest or start
    public Pair<Integer, Integer> getDest() {
        return new Pair<Integer, Integer>(dest.getKey(), dest.getValue());
    }
    public Pair<Integer, Integer> getStart() {
        return new Pair<Integer, Integer>(start.getKey(), start.getValue());
    }

    @Override
    public String toString() {
        return "Moving from " + convertPosToString(start) + " To " + convertPosToString(dest);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Move)) return false;

        Move m = (Move)o;
        return m.dest.equals(dest) && m.start.equals(start);
    }

    private String convertPosToString(Pair<Integer,Integer> pos) {
        char c = (char)(((char)'a') + (b.getYDim() - 1) - pos.getKey());
        return "" + c + (pos.getValue() + 1);
    }
}
