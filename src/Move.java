// TODO: descriptions everywhere?

import javafx.util.Pair;

public class Move {
    protected Pair<Integer, Integer> start;
    protected Pair<Integer, Integer> dest;

    protected int startDex;
    protected int endDex;

    protected Board b;

    protected boolean freshlyMoved; // stores whether or not this is the first move of the piece

    public Move(Board b, Pair<Integer,Integer> start, Pair<Integer,Integer> dest) {
        this.b = b;

        this.start = start;
        this.dest = dest;

        this.startDex = b.getIndexMap()[start.getKey()][start.getValue()];
        this.endDex = b.getIndexMap()[dest.getKey()][dest.getValue()];
    }

    // makes this move on the board, flagging certain variables if needed
    public void move() {
        ChessPiece moving = b.pieceAt(start);
        if(!moving.hasMoved()) {
            freshlyMoved = true;
            moving.setIfMoved(true);
        }
        if(moving instanceof ChessPieceKing) {
            b.updateKingPosition(moving.getSide(), dest);
        }
        if(moving instanceof ChessPiecePawn) {
            ChessPiecePawn p = (ChessPiecePawn)moving;
            p.checkIfUsedStartingMove(this);
        }

        b.setPieceLocation(start, -1);
        b.setPieceLocation(dest, startDex);
    }

    // undoes this move's change on the board
    // note that this method should only be used AFTER calling move()
    public void undoMove() {
        b.setPieceLocation(start, startDex);
        b.setPieceLocation(dest, endDex);

        ChessPiece moving = b.pieceAt(start);
        if(freshlyMoved) {
            b.pieceAt(start).setIfMoved(false);
        }
        if(moving instanceof ChessPieceKing) {
            b.updateKingPosition(moving.getSide(), start);
        }
        if(moving instanceof ChessPiecePawn) {
            ((ChessPiecePawn)moving).disableEnPasantFlag();
        }
    }

    // note that these getter do not allow aliases - purely a getter, no way to change dest or start
    public Pair<Integer, Integer> getDest() {
        return new Pair<>(dest.getKey(), dest.getValue());
    }
    public Pair<Integer, Integer> getStart() {
        return new Pair<>(start.getKey(), start.getValue());
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
