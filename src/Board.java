// the board class manages a chess board
// chess pieces are stored in an array which is accessed through a 2d array containing the chess grid

import java.util.LinkedList;
import java.util.Stack;

import javafx.geometry.Side;
import javafx.util.Pair;

public class Board {
    private int xDim = 8;
    private int yDim = 8;

    private int[][] indexMap; // represents the grid of the chess board and contains indices which reference the piece array
    private ChessPiece[] pieces;

    // stores a collection of past turns and where those attacks were to make looking for checks easier
    private Stack<boolean[][]> previousAttackBoards;

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
            pieces[pieceCounter + 4] = new King(side,  10);
            pieces[pieceCounter + 5] = new ChessPiece(side, "Bishop", 3, ChessPiece.BISHOP_MOVES);
            pieces[pieceCounter + 6] = new ChessPiece(side, "Knight", 3, ChessPiece.KNIGHT_MOVES);
            pieces[pieceCounter + 7] = new ChessPiece(side, "Rook", 5, ChessPiece.ROOK_MOVES);

            if(side) {
                whiteKingPos = new Pair<Integer, Integer>(row, 4);
            }else {
                blackKingPos = new Pair<Integer, Integer>(row, 4);
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
                pieces[pieceCounter] = new ChessPiece(side, "Pawn", 1, ChessPiece.DEFAULT_PAWN_MOVES);
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

        //indexMap[6][0] = -1;
        int dexVal = indexMap[whiteKingPos.getKey()][whiteKingPos.getValue()];
        indexMap[whiteKingPos.getKey()][whiteKingPos.getValue()] = -1;
        whiteKingPos = new Pair<Integer, Integer>(5,2);
        indexMap[whiteKingPos.getKey()][whiteKingPos.getValue()] = dexVal;

        dexVal = indexMap[0][3];
        indexMap[0][3] = -1;
        indexMap[3][4] = dexVal;
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

    protected int[][] getIndexMap() {
        return indexMap;
    }
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < yDim && col >= 0 && col < xDim;
    }

    public LinkedList<Move> generateMoves(boolean side) {
        LinkedList<Move> moves = new LinkedList<>();

        // note for working with checks - use attack boards to figure out where an opponent may attack


        for(int i = 0; i < indexMap.length; i++) {
            for(int k = 0; k < indexMap.length; k++) {
                ChessPiece piece = pieceAt(i, k);

                if(piece == null || piece.getSide() != side) continue; // skip over blank spaces + pieces not on side

                LinkedList<Move> pieceMoves = piece.generateMoves(this, new Pair<Integer, Integer>(i,k));

                // make sure the move doesn't lead into a checkmate
                for(Move m : pieceMoves) {
                    m.move();

                    boolean checkPresent = tileIsAttackedBySide(!side, getKingPosition(side));

                    m.undoMove();

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

                piece.recordMovementToAttackBoard(attackBoard, this, new Pair<Integer, Integer>(i, k));
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

                piece.recordMovementToAttackBoard(attackBoard, this, new Pair<Integer, Integer>(i, k));
                
                if(attackBoard[pos.getKey()][pos.getValue()]) return true; // attack is found
            }
        }

        return false;
    }
}