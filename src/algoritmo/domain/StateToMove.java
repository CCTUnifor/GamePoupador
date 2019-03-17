package algoritmo.domain;

import algoritmo.ActionEnum;

public class StateToMove {
    private ActionEnum action;
    private VisionEnum vision;

    private StateToMove() {
    }

    private StateToMove(ActionEnum action, VisionEnum vision) {
        this.action = action;
        this.vision = vision;
    }

    public ActionEnum getAction() {
        return action;
    }

    public VisionEnum getVision() {
        return vision;
    }

    public static StateToMove create(ActionEnum action, VisionEnum vision){
        return new StateToMove(action, vision);
    }

    @Override
    public String toString() {
        return action.toString() + " - " + vision.toString();
    }
}
