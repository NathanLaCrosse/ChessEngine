// the board class manages a chess board
// chess pieces are stored in an array which is accessed through a 2d array containing the chess grid

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import javafx.scene.image.WritableImage;
import javafx.util.Pair;

public class Board {
    // the following static data members are needed to allow for new boards and pieces to be added on top of the traditional chess game
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
    private static HashMap<String, ChessPiece> VANILLA_PIECE_LOOKUP = buildLookup();
    private static HashMap<String, ChessPiece> buildLookup() {
        HashMap<String, ChessPiece> pieceMap = new HashMap<>();

        pieceMap.put("Pawn", new ChessPiecePawn(true));
        pieceMap.put("Bishop", new ChessPiece(true, "Bishop", 3, ChessPiece.BISHOP_MOVES));
        pieceMap.put("Knight", new ChessPiece(true, "Knight", 3, ChessPiece.KNIGHT_MOVES));
        pieceMap.put("Rook", new ChessPiece(true, "Rook", 5, ChessPiece.ROOK_MOVES));
        pieceMap.put("Queen", new ChessPiece(true, "Queen", 9, ChessPiece.QUEEN_MOVES));
        pieceMap.put("King", new ChessPieceKing(true, 10));

        return pieceMap;
    }
    public static HashMap<String, ChessPiece> cloneVanillaPieceLookup() {
        HashMap<String, ChessPiece> clone = new HashMap<>();
        for(Entry<String, ChessPiece> pair : VANILLA_PIECE_LOOKUP.entrySet()) {
            clone.put(pair.getKey(), pair.getValue());
        }
        return clone;
    }
    // sprites for any custom pieces
    private HashMap<String, Pair<WritableImage, WritableImage>> imageLookup;
    public HashMap<String, Pair<WritableImage, WritableImage>> getImageLookup() {return imageLookup;}

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

    public Board(String[][] boardLayout, HashMap<String, ChessPiece> pieceLookup, HashMap<String, Pair<WritableImage, WritableImage>> imageLookup) {
        indexMap = new int[8][8];

        // determine how many pieces are on the board
        int totalPieces = 0;
        for(String[] row : boardLayout) {
            for(String str : row) {
                if(!str.equals("Blank")) totalPieces++;
            }
        }
        pieces = new ChessPiece[totalPieces];

        int pieceCounter = 0;
        for(int i = 0; i < 8; i++) {
            boolean side = i < 4 ? false : true;
            for(int k = 0; k < 8; k++) {
                String str = boardLayout[i][k];

                if(str.equals("Blank")) {
                    indexMap[i][k] = -1;
                }else {
                    ChessPiece foundPiece = pieceLookup.get(str); // grab piece to copy
                    ChessPiece newPiece;
                    if(foundPiece instanceof ChessPieceKing) {
                        newPiece = new ChessPieceKing(side, 10);
                        if(side) {
                            whiteKingPos = new Pair<>(i, k);
                        }else {
                            blackKingPos = new Pair<>(i, k);
                        }
                    }else if(foundPiece instanceof ChessPiecePawn) {
                        newPiece = new ChessPiecePawn(side);
                    }else {
                        // build a copy of this piece's moves
                        String[] copyMoves = new String[foundPiece.getNumMoves()];
                        for(int j = 0; j < copyMoves.length; j++) {copyMoves[j] = foundPiece.getMove(j);}
                        newPiece = new ChessPiece(side, str, foundPiece.getMaterial(), copyMoves);
                    }

                    pieces[pieceCounter] = newPiece;
                    indexMap[i][k] = pieceCounter;
                    pieceCounter++;
                }
            }
        }

        this.imageLookup = imageLookup;
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