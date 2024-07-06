// actually makes illegal moves :)
public class HorribleBot extends Entity {

    public HorribleBot(boolean side) {
        super(side, "Horrible Bot");
    }

    @Override
    public Move selectMove(Board b) {
        return null;
    }

    @Override
    public void reset() {

    }  
    
}
