package model;

import java.awt.*;
import java.util.Comparator;

public class StateToGo {
    private ActionEnum action;
    private Point position;
    private MapEnum mapEnum;
    private double weight = 0;

    public StateToGo(ActionEnum action, Point position) {
        this.action = action;
        this.position = position;
    }

    public StateToGo(ActionEnum action, Point position, MapEnum mapEnum) {
        this.action = action;
        this.position = position;
        this.mapEnum = mapEnum;
    }

    public ActionEnum getAction() { return action; }

    public Point getPosition() { return position; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public static Comparator<StateToGo> comparator() {
        return new Comparator<StateToGo>() {
            public int compare(StateToGo state0, StateToGo state1) {
                double weight0 = state0.getWeight();
                double weight1 = state1.getWeight();

                return weight0 > weight1 ? 1 : weight0 < weight1 ? -1 : 0;
            }
        };
    }

    @Override
    public String toString() {
        return position.toString() + " - " + action.toString() + " - " + mapEnum + " - " + weight;
    }
}
