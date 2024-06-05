import java.util.LinkedList;

import javafx.util.Pair;

public class ChessPiece {
    public static final String[] KNIGHT_MOVES = new String[]{"C|NNW","C|NNE","C|EEN","C|EES","C|SSW","C|SSE","C|WWN","C|WWS"};
    public static final String[] ROOK_MOVES = new String[]{"CL|N","CL|S","CL|E","CL|W"};
    public static final String[] BISHOP_MOVES = new String[]{"CL|NW","CL|NE","CL|SE","CL|SW"};
    public static final String[] QUEEN_MOVES = new String[]{"CL|N","CL|S","CL|E","CL|W","CL|NW","CL|NE","CL|SE","CL|SW"};

    public static final String[] DEFAULT_PAWN_MOVES = new String[]{"SI2|N","O|NW","O|NE"};
    public static final String[] DEFAULT_KING_MOVES = new String[]{"C|N","C|NE","C|E","C|SE","C|S","C|SW","C|W","C|NW"};

    private boolean side;
    private String name;
    private int material;
    private String[] moveInstructions;

    private boolean hasMoved;

    public ChessPiece(boolean side, String name, int material, String[] moveInstructions) {
        this.side = side;
        this.name = name;
        this.material = material;
        this.moveInstructions = moveInstructions;
        hasMoved = false;
    }

    public boolean getSide() {
        return side;
    }
    public String getName() {
        return name;
    }
    public int getMaterial() {
        return material;
    }
    public boolean hasMoved() {
        return hasMoved;
    }

    protected void setIfMoved(boolean state) {
        this.hasMoved = state;
    }

    public LinkedList<Move> generateMoves(Board b, Pair<Integer, Integer> location) {
        LinkedList<Move> moves = new LinkedList<>();

        for(int i = 0; i < moveInstructions.length; i++) {
            boolean nonLoopingFlag = false;

            String modifier = moveInstructions[i].substring(0, moveInstructions[i].indexOf("|")); // informs us of modifiers needed
            String directions = moveInstructions[i].substring(moveInstructions[i].indexOf("|") + 1); // path of move

            // check for modifier statements
            // each modifier has its own unique character 
            boolean canCapture = modifier.indexOf("C") != -1;
            boolean onlyCapture = modifier.indexOf("O") != -1;
            boolean loop = modifier.indexOf("L") != -1;
            boolean startingMove = modifier.indexOf("S") != -1;

            // example sequence modifier: I3|N 
            // goes north 3 times
            int sequence = 0;
            int sequenceCounter = 0;
            int sequenceDex = modifier.indexOf("I");
            if(sequenceDex != -1) {
                sequence = Integer.parseInt(modifier.substring(sequenceDex + 1, sequenceDex + 2));
            }

            // a starting sequence repeats only when it hasnt moved (there will still be one move if it has moved) ex: SI3|NE
            boolean startingSequence = modifier.indexOf("SI") != -1;

            if(!startingSequence && startingMove && !hasMoved()) continue; // skip over this starting move if we've already moved (excludes a starting sequence)

            // find a possible move but stop if not looping/come into contact with another piece
            Pair<Integer, Integer> dest = findDestCoords(directions, location);
            while(b.inBounds(dest.getKey(), dest.getValue()) && (!nonLoopingFlag || loop || (sequenceCounter < sequence && (!startingSequence || !hasMoved())))) {
                ChessPiece destPiece = b.pieceAt(dest);

                if(destPiece == null) {
                    if(onlyCapture) break;

                    moves.add(new Move(b, location, dest));
                }else if(destPiece.side != side && (canCapture || onlyCapture)) {
                    moves.add(new Move(b, location, dest));
                    
                    break;
                }else {
                    break;
                }

                nonLoopingFlag = true;
                if(loop || sequenceCounter < sequence) {
                    dest = findDestCoords(directions, dest);
                }

                sequenceCounter++;
            }
        }

        return moves;
    }

    // same process as with generating moves but instead we the value at each move's destination to true, showing there is an attack there
    public void recordMovementToAttackBoard(boolean[][] attackBoard, Board b, Pair<Integer, Integer> location) {
        for(int i = 0; i < moveInstructions.length; i++) {
            boolean nonLoopingFlag = false;

            String modifier = moveInstructions[i].substring(0, moveInstructions[i].indexOf("|")); // informs us of modifiers needed
            String directions = moveInstructions[i].substring(moveInstructions[i].indexOf("|") + 1); // path of move

            // check for modifier statements
            // each modifier has its own unique character 
            boolean canCapture = modifier.indexOf("C") != -1;
            boolean onlyCapture = modifier.indexOf("O") != -1;
            boolean loop = modifier.indexOf("L") != -1;
            boolean startingMove = modifier.indexOf("S") != -1;

            // example sequence modifier: I3|N 
            // goes north 3 times
            int sequence = 0;
            int sequenceCounter = 0;
            int sequenceDex = modifier.indexOf("I");
            if(sequenceDex != -1) {
                sequence = Integer.parseInt(modifier.substring(sequenceDex + 1, sequenceDex + 2));
            }

            // a starting sequence repeats only when it hasnt moved (there will still be one move if it has moved) ex: SI3|NE
            boolean startingSequence = modifier.indexOf("SI") != -1;

            if(!startingSequence && startingMove && !hasMoved()) continue; // skip over this starting move if we've already moved (excludes a starting sequence)

            // find a possible move but stop if not looping/come into contact with another piece
            Pair<Integer, Integer> dest = findDestCoords(directions, location);
            while(b.inBounds(dest.getKey(), dest.getValue()) && (!nonLoopingFlag || loop || (sequenceCounter < sequence && (!startingSequence || !hasMoved())))) {
                ChessPiece destPiece = b.pieceAt(dest);

                // there is a difference here - we must be able to capture for a move that doesn't capture to 
                // be recorded on the attack board, as these will be useful for detecting checks
                if(destPiece == null && canCapture) {
                    if(onlyCapture) break;

                    attackBoard[dest.getKey()][dest.getValue()] = true;
                }else if(destPiece != null && destPiece.side != side && (canCapture || onlyCapture)) {
                    attackBoard[dest.getKey()][dest.getValue()] = true;
                    
                    break;
                }else {
                    break;
                }

                nonLoopingFlag = true;
                if(loop || sequenceCounter < sequence) {
                    dest = findDestCoords(directions, dest);
                }

                sequenceCounter++;
            }
        }
    }

    // find the destination of a given instruction
    // traverse through cardinal directions
    private Pair<Integer, Integer> findDestCoords(String instruction, Pair<Integer, Integer> start) {
        int row = start.getKey();
        int col = start.getValue();

        for(int i = 0; i < instruction.length(); i++) {
            char c = instruction.charAt(i);

            switch (c) {
                case 'N':
                    if(side) {
                        row--;
                    }else {
                        row++;
                    }
                    break;
                case 'S':
                    if(side) {
                        row++;
                    }else {
                        row--;
                    }
                    break;
                case 'E':
                    if(side) {
                        col++;
                    }else {
                        col--;
                    }
                    break;
                default: // 'W' case (default = else)
                    if(side) {
                        col--;
                    }else {
                        col++;
                    }
                    break;
            }
        }

        return new Pair<Integer, Integer>(row, col);
    }
}
