// the entity class defines what a "player" will be able to do in the game
// examples of entities are human players and bots

public abstract class Entity {
    private String name; // used to give bots a name
    private boolean side;

    public Entity(boolean side, String name) {
        this.side = side;
        this.name = name;
    }

    public boolean getSide() {
        return side;
    }
    public String getName() {
        return name;
    }

    // this method will be defined by derived class to give a unique way to select a "best" move
    public abstract Move selectMove(Board b);

    // override this method if any variables need resetting after a move is played
    public abstract void reset();
}
