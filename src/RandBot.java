// the most basic of chess bots - plays a random move

import java.util.LinkedList;

public class RandBot extends Entity{
    
    public RandBot(boolean side) {
        super(side);
    }

    @Override
    public Move selectMove(Board b) {
        LinkedList<Move> validMoves = b.generateMoves(getSide());
        int randIndex = (int)(Math.random() * validMoves.size());
        return validMoves.get(randIndex);
    }

    @Override
    public void reset() {

    }
}
