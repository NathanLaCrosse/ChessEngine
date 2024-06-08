// meant to be a simple chess bot that can still win games - at least against rand bot
// TODO: This bot may become stronger than what we want players to face...

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeMap;

public class StageOneBot extends Entity {
    private Move moveToPlay;

    public StageOneBot(boolean side) {
        super(side);
    }

    // no resetting of variables is necessary
    @Override
    public void reset() {
        
    }

    @Override
    public Move selectMove(Board b) {
        moveToPlay = null;

        search(b, getSide(), 6);

        return moveToPlay;
    }
    
    // for search methods: they employ something called alpha-beta pruning which lets us search through all possible moves more efficiently
    // by removing trees of moves that are worse than ones already calculated
    private double search(Board b, boolean sideToPlay, int depth) {return search(b, sideToPlay, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);}
    private double search(Board b, boolean sideToPlay, int depth, double alpha, double beta, int distFromRoot) {
        if(depth <= 0) return gradeBoardState(b, sideToPlay); // once we've reached our depth, grade the board

        // check for a game end
        int endCon = b.getEndCondition(sideToPlay);
        if(endCon == 2) return Integer.MIN_VALUE; // we are checkmated
        if(endCon != -1) return -1000; // some kind of draw

        Collection<Move> orderedMoves = createOrderedMoves(b, sideToPlay);

        double bestMoveScore = Integer.MIN_VALUE;
        double eval;

        for(Move m : orderedMoves) {
            m.move();
            eval = -search(b, !sideToPlay, depth-1, -beta, -alpha, distFromRoot + 1);
            m.undoMove();

            // give a boost based on the moves strength to support short-term plays 
            if(distFromRoot == 0) {
                eval += 2 * gradeMove(b, m);
            }

            if(eval > bestMoveScore) {
                bestMoveScore = eval;

                if(distFromRoot == 0) moveToPlay = m;

                alpha = Math.max(alpha, eval);
                if(alpha >= beta) {break;} // exit bad move trees
            }
        }

        return bestMoveScore;
    }

    // orders moves in such a way so that we are more likely to find the best move first, which speeds up the calculation
    // puts moves in a map that is automatically sorted
    private Collection<Move> createOrderedMoves(Board b, boolean side) {
        LinkedList<Move> validMoves = b.generateMoves(side);
        TreeMap<Double, Move> orderedMoves = new TreeMap<>();

        for(Move m : validMoves) {
            orderedMoves.put(gradeMove(b, m), m);
        }

        return orderedMoves.values();
    }

    private double gradeBoardState(Board b, boolean side) {
        double score = 0;

        // grade material advantage
        int materialAdvantage = b.materialTotalForSide(side) - b.materialTotalForSide(!side);
        score += materialAdvantage * 100;

        // we want our opponent in check but not us
        if(b.inCheck(side)) {
            score -= 200;
        }else if(b.inCheck(!side)) {
            score += 200;
        }

        //check how many moves their king has - the less, the better
        LinkedList<Move> enemyMoves = b.generateMoves(!side);
        for(Move m : enemyMoves) {
            if(m.getMovingPiece().getName().equals("King")) score -= 40;
        }

        return score;
    }

    // note that grading the move causes the move to happen for a brief moment
    // DO NOT MAKE THE MOVE (WITHOUT UNMOVING) BEFORE CALLING THIS METHOD
    private double gradeMove(Board b, Move m) {
        double score = 0;
        boolean side = m.getSide();

        m.move();

        // perform checks that require the move made
        boolean destIsAttacked = b.tileIsAttackedBySide(!side, m.getDest());

        m.undoMove();

        ChessPiece moving = m.getMovingPiece();
        ChessPiece capture = m.getCapturedPiece();
        if(capture != null) {
            //increase score if it is a positive trade or equal trade - don't make a negative trade
            int materialDif = capture.getMaterial() - moving.getMaterial();
            if(materialDif >= 0) {
                score += 4 + 2 * materialDif;
            }
            // piece is hanging - take it based on how good it is
            if(!destIsAttacked) {
                score += 10 * capture.getMaterial();
            }else if(capture.getName().equals("King")) {
                score += 30; // checks are good
            }
        }

        // // give boost to get off an attacked square
        // if(b.tileIsAttackedBySide(!side, m.getStart())) {
        //     score += moving.getMaterial();
        // }

        // // encourage early-game pawn movement
        // if(moving.getName().equals("Pawn") && b.getMovesMade() < 4) {
        //     score += 7;
        // }

        // // encourage moving unmoved pieces (as long as they aren't kings or rooks because of castling)
        // if(!moving.hasMoved() && !moving.getName().equals("Rook") && !moving.getName().equals("King")) {
        //     score += 20;
        // }

        return score;
    }
}
