package algoritmo.domain;

import algoritmo.ActionEnum;

import java.awt.*;

public class StateToMove {
    private ActionEnum action;
    private VisionEnum vision;
    private Point position;
    private boolean hasCoin;

    private StateToMove() {
    }

    private StateToMove(ActionEnum action, VisionEnum vision, Point position) {
        this.action = action;
        this.vision = vision;
        this.position = position;
    }

    public ActionEnum getAction() { return action; }
    public VisionEnum getVision() { return vision; }
    public Point getPosition() { return position; }

    public boolean hasCoins() { return hasCoin; }
    public void setHasCoins(boolean hasCoins) { this.hasCoin = hasCoins; }

    public static StateToMove create(ActionEnum action, VisionEnum vision, Point position){
        return new StateToMove(action, vision, position);
    }

    @Override
    public String toString() {
        return action.toString() + " - " + vision.toString() + " - " + position.toString() + (hasCoins() ? " - tem moeda por aqui" : "");
    }
}
