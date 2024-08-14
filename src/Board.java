// the board class manages a chess board
// chess pieces are stored in an array which is accessed through a 2d array containing the chess grid

import java.util.LinkedList;
import javafx.util.Pair;

public class Board {
    public static final String[][] DEFAULT_BOARD_REP = {
        {"Rook","Knight","Bishop","Queen","King","Bishop","Knight","Rook"},
        {"Pawn","Pawn","Pawn","Pawn","Pawn","Pawn","Pawn","Pawn"},
        {"Blank","Blank","Blank","Blank","Blank","Blank","Blank","Blank"},
        {"Blank","Blank","Blank","Blank","Blank","Blank","Blank","Blank"},
        {"Blank","Blank","Blank","Blank","Blank","Blank","Blank","Blank"},
        {"Blank","Blank","Blank","Blank","Blank","Blank","Blank","Blank"},
        {"Pawn","Pawn","Pawn","Pawn","Pawn","Pawn","Pawn","Pawn"},
        {"Rook","Knight","Bishop","Queen","King","Bishop","Knight","Rook"}
    };

    // a chess bot will not be able to alter the counter
    protected int fiftyMoveCounter = 0;
    private int movesMade = 0;
    private int illegalStateSide = -1;

    private int xDim = 8;
    private int yDim = 8;

    private int[][] indexMap; // represents the grid of the chess board and contains indices which reference the piece array
    private ChessPiece[] pieces;

    // keeps track of the kings
    private Pair<Integer, Integer> whiteKingPos;
    private Pair<Integer, Integer> blackKingPos;

    public Board() {
        indexMap = new int[8][8];
        pieces = new ChessPiece[32];

        int pieceCounter = 0;

        // add populated rows first
        for(int i = 0; i < 2; i++) {
            boolean side = i == 0;
            int row = side ? 7 : 0;

            pieces[pieceCounter] = new ChessPiece(side, "Rook", 5, ChessPiece.ROOK_MOVES);
            pieces[pieceCounter + 1] = new ChessPiece(side, "Knight", 3, ChessPiece.KNIGHT_MOVES);
            pieces[pieceCounter + 2] = new ChessPiece(side, "Bishop", 3, ChessPiece.BISHOP_MOVES);
            pieces[pieceCounter + 3] = new ChessPiece(side, "Queen", 9, ChessPiece.QUEEN_MOVES);
            pieces[pieceCounter + 4] = new ChessPieceKing(side,  10);
            pieces[pieceCounter + 5] = new ChessPiece(side, "Bishop", 3, ChessPiece.BISHOP_MOVES);
            pieces[pieceCounter + 6] = new ChessPiece(side, "Knight", 3, ChessPiece.KNIGHT_MOVES);
            pieces[pieceCounter + 7] = new ChessPiece(side, "Rook", 5, ChessPiece.ROOK_MOVES);

            if(side) {
                whiteKingPos = new Pair<>(row, 4);
            }else {
                blackKingPos = new Pair<>(row, 4);
            }

            // set up indexes for row
            for(int k = 0; k < 8; k++) {
                indexMap[row][k] = pieceCounter;
                pieceCounter++;
            }
        }

        // fill in pawns
        for(int i = 0; i < 2; i++) {
            boolean side = i == 0;
            int row = side ? 6 : 1;

            for(int k = 0; k < 8; k++) {
                pieces[pieceCounter] = new ChessPiecePawn(side);
                indexMap[row][k] = pieceCounter;
                pieceCounter++;
            }
        }

        // set all other spaces to -1
        for(int i = 2; i < 6; i++) {
            for(int k = 0; k < 8; k++) {
                indexMap[i][k] = -1;
            }
        }

        // test code
        // indexMap[6][0] = -1;
        // int dexVal = indexMap[whiteKingPos.getKey()][whiteKingPos.getValue()];
        // indexMap[whiteKingPos.getKey()][whiteKingPos.getValue()] = -1;
        // whiteKingPos = new Pair<>(5,2);
        // indexMap[whiteKingPos.getKey()][whiteKingPos.getValue()] = dexVal;

        // dexVal = indexMap[0][3];
        // indexMap[0][3] = -1;
        // indexMap[3][4] = dexVal;
    }

    public int getXDim() {
        return xDim;
    }
    public int getYDim() {
        return yDim;
    }

    // allows for moves to update the king's position when moved
    protected void updateKingPosition(boolean side, Pair<Integer, Integer> newPos) {
        if(side) {
            whiteKingPos = newPos;
        }else {
            blackKingPos = newPos;
        }
    }
    private Pair<Integer, Integer> getKingPosition(boolean side) {
        if(side) {
            return whiteKingPos;
        }else {
            return blackKingPos;
        }
    }

    // allows for outside methods to change where pieces are on the board
    // only meant to be used in the move class and its subclasses
    protected void setPieceLocation(Pair<Integer, Integer> pos, int indexVal) {
        indexMap[pos.getKey()][pos.getValue()] = indexVal;
    }
    public ChessPiece pieceAt(int row, int col) {
        if(indexMap[row][col] == -1) return null;
        return pieces[indexMap[row][col]];
    }
    public ChessPiece pieceAt(Pair<Integer, Integer> pos) {
        return pieceAt(pos.getKey(), pos.getValue());
    }

    protected void incrementMovesMade() {
        movesMade++;
    }
    protected void decrementMovesMade() {
        movesMade--;
    }
    public int getMovesMade() {
        return movesMade;
    }

    protected int[][] getIndexMap() {
        return indexMap;
    }
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < yDim && col >= 0 && col < xDim;
    }
    public boolean inBounds(Pair<Integer, Integer> pos) {
        return inBounds(pos.getKey(), pos.getValue());
    }
    public boolean inPromotionRank(boolean side, Pair<Integer, Integer> pos) {
        if(side) {
            return pos.getKey() == 0;
        }else {
            return pos.getKey() == yDim - 1;
        }
    }
    public int materialTotalForSide(boolean side) {
        int total = 0;
        for(int i = 0; i < indexMap.length; i++) {
            for(int k = 0; k < indexMap[0].length; k++) {
                ChessPiece pieceAt = pieceAt(i, k);
                if(pieceAt != null && pieceAt.getSide() == side && !pieceAt.getName().equals("King")) {
                    total += pieceAt.getMaterial();
                }
            }
        }
        return total;
    }
    public Pair<Integer, Integer> getKingPosOnSide(boolean side) {
        if(side) {
            return whiteKingPos;
        }else {
            return whiteKingPos;
        }
    }

    public LinkedList<Move> generateMoves(boolean side) {
        LinkedList<Move> moves = new LinkedList<>();

        for(int i = 0; i < indexMap.length; i++) {
            for(int k = 0; k < indexMap.length; k++) {
                ChessPiece piece = pieceAt(i, k);

                if(piece == null || piece.getSide() != side) continue; // skip over blank spaces + pieces not on side

                LinkedList<Move> pieceMoves = piece.generateMoves(this, new Pair<>(i,k)); 

                // make sure the move doesn't lead into a checkmate
                for(Move m : pieceMoves) {
                    m.move();

                    boolean checkPresent = tileIsAttackedBySide(!side, getKingPosition(side));

                    m.undoMove();

                    // make sure a piece doesn't intercept a castle
                    if(!checkPresent && m instanceof MoveCastle && !inCheck(side)) {
                        checkPresent = tileIsAttackedBySide(!side, ((MoveCastle)m).getRookDest());
                    }

                    if(!checkPresent) {
                        moves.add(m);
                    }
                }
            }
        }

        return moves;
    }

    public boolean[][] createAttackBoardForSide(boolean side) {
        boolean[][] attackBoard = new boolean[yDim][xDim];

        for(int i = 0; i < indexMap.length; i++) {
            for(int k = 0; k < indexMap[0].length; k++) { 
                if(indexMap[i][k] == -1) continue; // skip empty spaces
                ChessPiece piece = pieces[indexMap[i][k]];
                if(piece.getSide() != side) continue; // skip over pieces not on our side

                piece.recordMovementToAttackBoard(attackBoard, this, new Pair<>(i, k));
            }
        }

        return attackBoard;
    }

    // faster version of creating an attack board - process is cut off early
    // if the attack is found quickly
    public boolean tileIsAttackedBySide(boolean side, Pair<Integer, Integer> pos) {
        boolean[][] attackBoard = new boolean[yDim][xDim];

        for(int i = 0; i < indexMap.length; i++) {
            for(int k = 0; k < indexMap[0].length; k++) {
                if(indexMap[i][k] == -1) continue; // skip empty spaces
                ChessPiece piece = pieces[indexMap[i][k]];
                if(piece.getSide() != side) continue; // skip over opponent's pieces

                piece.recordMovementToAttackBoard(attackBoard, this, new Pair<>(i, k));
                
                if(attackBoard[pos.getKey()][pos.getValue()]) return true; // attack is found
            }
        }

        return false;
    }

    // returns true if the given piece can attack the given tile
    public boolean pieceAttacksTile(ChessPiece piece, Pair<Integer, Integer> piecePos, Pair<Integer, Integer> tilePos) {
        boolean[][] attackBoard = new boolean[yDim][xDim];

        piece.recordMovementToAttackBoard(attackBoard, this, piecePos);

        return attackBoard[tilePos.getKey()][tilePos.getValue()];
    }

    // checks the board to determine if the inputted side is in a possible end state
    // the possible end states are as follows (no repeat board rule)
    // -1 - game has not ended
    // 0 - fifty move rule
    // 1 - insufficient material
    // 2 - checkmate
    // 3 - stalemate
    public int getEndCondition(boolean side) {
        if(illegalStateSide != -1) return 4; // illegal board state

        if(fiftyMoveRuleValid()) return 0; // fifty move rule
        if(insufficientMaterial()) return 1; // insufficient material

        // look at the amount of moves available to this side
        LinkedList<Move> availableMoves = generateMoves(side);

        if(availableMoves.size() == 0) {
            if(inCheck(side)) {
                return 2; // checkmate
            }else {
                return 3; // stalemate
            }
        }else {
            return -1; // game has not ended
        }
    }

    // game is a draw if after 50 moves there hasn't been a capture
    public boolean fiftyMoveRuleValid() {
        return fiftyMoveCounter == 50;
    }
    // returns true if there is insufficient material on the board - checkmate is impossible
    public boolean insufficientMaterial() {
        // to begin, insufficient material can occur only when both sides have only 1 (or less) piece (besides king)
        ChessPiece onlyWhitePiece = null;
        Pair<Integer, Integer> whitePos = null;
        ChessPiece onlyBlackPiece = null;
        Pair<Integer, Integer> blackPos = null;

        for(int i = 0; i < indexMap.length; i++) {
            for(int k = 0; k < indexMap[0].length; k++) {
                ChessPiece pieceAt = pieceAt(i, k);
                if(pieceAt != null) {
                    if(pieceAt.getSide()) {
                        if(onlyWhitePiece != null) return false; // more than one white piece
                        else if(!pieceAt.getName().equals("King")) {
                            onlyWhitePiece = pieceAt;
                            whitePos = new Pair<>(i, k);
                        }
                    }else {
                        if(onlyBlackPiece != null) return false; // more than one black piece 
                        else if(!pieceAt.getName().equals("King")) {
                            onlyBlackPiece = pieceAt;
                            blackPos = new Pair<>(i, k);
                        }
                    }
                }
            }
        }

        if(onlyWhitePiece == null && onlyBlackPiece == null) return true; // only two kings left

        // if both sides have a piece, if they are both bishops on the same colored square, there is insufficient material
        if(onlyWhitePiece != null && onlyBlackPiece != null) {
            if(!onlyWhitePiece.getName().equals("Bishop") || !onlyBlackPiece.getName().equals("Bishop")) return false; // return if both aren't bishops

            return (whitePos.getKey() + whitePos.getValue()) % 2 == (blackPos.getKey() + blackPos.getValue()) % 2; // return true if on same-colored square
        }

        // past this point there is only one piece to look at - either a white or black piece
        ChessPiece lastPiece = onlyWhitePiece != null ? onlyWhitePiece : onlyBlackPiece;

        // stalemate if this last piece is a knight or bishop
        return lastPiece.getName().equals("Bishop") || lastPiece.getName().equals("Knight");
    }
    // returns true if the given side is in check
    public boolean inCheck(boolean side) {
        return tileIsAttackedBySide(!side, side ? whiteKingPos : blackKingPos);
    }

    // flags if a side has made an illegal move
    protected void flagIllegalMove(boolean side) {
        illegalStateSide = side ? 1 : 2;
    }
}