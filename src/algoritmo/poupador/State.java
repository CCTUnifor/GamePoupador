package algoritmo.poupador;

import java.awt.Point;
import java.util.Comparator;

import algoritmo.poupador.enums.EAction;

public class State{
	private EAction action;
	private Point position;
	private double weight = 0;
	
	public State(EAction action, Point position) {
		this.action = action;
		this.position = position;
	}

	public EAction getAction() { return action; }

	public Point getPosition() { return position; }

	public double getWeight() { return weight; }
	public void setWeight(double weight) { this.weight = weight; }

	public static Comparator<State> comparator() {
		return new Comparator<State>() {			
			public int compare(State state0, State state1) {
				double weight0 = state0.getWeight();
				double weight1 = state1.getWeight();
				
				return weight0 > weight1 ? 1 : weight0 < weight1 ? -1 : 0;
			}
		};
	}
}
