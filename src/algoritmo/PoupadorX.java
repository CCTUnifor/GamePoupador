package algoritmo;

import controle.Constantes;

import java.awt.Point;
import java.util.*;
import java.util.Map.Entry;

public class PoupadorX extends ProgramaPoupador {
	public static int BANK_X = -1;
	public static int BANK_Y = -1;
	private static final int VISION_MATRIX_SIZE = 5;
	private static final int SMELL_MATRIX_SIZE = 3;
	private static final int MAP_M = 30;
	private static final int MAP_N = 30;
	private static final int POWER_UP_PRICE = 5;

	private int[][] map;
	private int[][] vision;
	private int[][] smell;
	private boolean escaping = false;
	private int money;
	private Point currentPosition;
	private boolean firstIteration = true;
	private Hashtable<Integer, Point> agentsMap;
	private int[][] walkMemory;
	private int attemptsToPickUpPower;

	private void instanciation() {
		map = new int[MAP_M][MAP_N];
		undiscoverMap();
		firstIteration = false;
		walkMemory = new int[MAP_M][MAP_N];
	}

	private void undiscoverMap() {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				map[i][j] = EMapCode.UNKNOW_CELL.getValue();
			}
		}
	}

	public int acao() {
		updateMap();

		List<State> newPossibleStates = getStateSuccessors(this.currentPosition);

		for(State s : newPossibleStates)
			s.setWeight(getStateWeight(s));

		Collections.shuffle(newPossibleStates);
		Collections.sort(newPossibleStates, State.comparator());

		if(!newPossibleStates.isEmpty()) {
			State value = newPossibleStates.get(newPossibleStates.size() - 1);
			if (map[value.getPosition().y][value.getPosition().x] == EMapCode.POWER_UP.getValue())
				attemptsToPickUpPower = 0;

			return value.getAction().getValue();
		}

		return EAction.STOP.getValue();
	}

	private double getStateWeight(State s) {
		double weight = 0;

		weight = calcVisionWeight(s);
		weight += calcSmellWeight(s);
		weight += calcExplorationWeight(s);

		return weight;
	}

	private void updateMap() {
		if (firstIteration)
			instanciation();
		 else
			clearAgentsFromMap(currentPosition);

		agentsMap = new Hashtable<Integer, Point>();

		currentPosition = sensor.getPosicao();
		walkMemory[currentPosition.y][currentPosition.x] += 5;


		vision = Util.getSensorArrayAsMatrix(sensor.getVisaoIdentificacao(), VISION_MATRIX_SIZE, EMapCode.SELF_POSITION.getValue());
		smell = Util.getSensorArrayAsMatrix(sensor.getAmbienteOlfatoLadrao(), SMELL_MATRIX_SIZE, EMapCode.SELF_POSITION.getValue());

		money = sensor.getNumeroDeMoedas();
		fillVisualMap(vision);

		if (PoupadorX.BANK_X != -1 && PoupadorX.BANK_Y != -1)
			map[PoupadorX.BANK_X][PoupadorX.BANK_Y] = EMapCode.BANK.getValue();

		escaping = seeingThief();
	}

	private boolean seeingThief() {
		for (Entry<Integer, Point> tuple : agentsMap.entrySet()) {
			Integer agentKey = tuple.getKey();

			if(agentKey >= EMapCode.THIEF.getValue())
				return true;
		}

		return false;
	}

	private void fillVisualMap(int[][] sensor) {
		int offset = VISION_MATRIX_SIZE/2;
		int m, n = m = VISION_MATRIX_SIZE;

		for (int i = this.currentPosition.y - offset, count_i = 0; count_i < m; count_i++, i++) {
			for (int j = this.currentPosition.x - offset, count_j = 0; count_j < n; count_j++, j++) {
				int cellValue = sensor[count_i][count_j];

				if (Util.isInMap(map, i, j) && cellValue != -2) {
					this.map[i][j] = cellValue;
					if (cellValue == EMapCode.BANK.getValue()){
						PoupadorX.BANK_X = i;
						PoupadorX.BANK_Y = j;
					}

					if (cellValue >= EMapCode.THIEF.getValue()) {
						agentsMap.put(cellValue, new Point(j, i));
					} else if (cellValue >= EMapCode.SAVER.getValue()) {
						agentsMap.put(cellValue, new Point(j, i));
					}
				}
			}
		}
	}

	private void clearAgentsFromMap(Point oldPosition) {
		if (agentsMap != null) {
			for (Entry<Integer, Point> tuple : agentsMap.entrySet()) {
				Point point = tuple.getValue();

				this.map[point.y][point.x] = 0;
			}
		}

		if(oldPosition != null)
			this.map[oldPosition.y][oldPosition.x] = 0;
	}

	private boolean shouldIBuyThePowerUp() {
		return (escaping || attemptsToPickUpPower >= 3) && money > POWER_UP_PRICE && sensor.getNumeroJogadasImunes() == 0;
	}

	private boolean haveMoney() {
		return money > 0;
	}

	private List<State> getStateSuccessors(Point point){
		List<State> validStates = new ArrayList<State>();

		State[] nextStates = {
				new State(EAction.UP, new Point(point.x, point.y - 1)),
				new State(EAction.DOWN, new Point(point.x, point.y + 1)),
				new State(EAction.RIGHT, new Point(point.x + 1, point.y)),
				new State(EAction.LEFT, new Point(point.x - 1, point.y))
		};

		for(State s : nextStates) {
			int x = s.getPosition().x;
			int y = s.getPosition().y;
			if(Util.isInMap(map, s.getPosition())) {
				if (Util.isWalkable(map, s.getPosition(), shouldIBuyThePowerUp(), haveMoney()))
					validStates.add(s);
				if(map[y][x] == EMapCode.POWER_UP.getValue())
					attemptsToPickUpPower++;
			}
		}

		return validStates;
	}

	private double calcExplorationWeight(State s) {
		double weight = 0;

		SubMatrix submatrix = Util.cutMatrix(map, currentPosition, s.getAction());

		for(int i = submatrix.i; i < submatrix.m; i++) {
			for(int j = submatrix.j; j < submatrix.n; j++) {
				int cellMap = map[i][j];

				if(cellMap != 0) {
					double distance = Util.getDistance(s.getPosition(), j, i);
					double iterationWeight = identifyWeightByCode(cellMap) / 10;

					if(distance == 0)
						weight += iterationWeight ;
					else
						weight += iterationWeight  / distance;
				}

			}
		}

		weight -= walkMemory[s.getPosition().y][s.getPosition().x];

		return weight;
	}

	private float calcSmellWeight(State s) {
		float weight = 0;

		switch(s.getAction()) {
			case UP: for (int i = 0; i < smell.length; i++) weight += smell[0][i];
				break;
			case DOWN: for (int i = 0; i < smell.length; i++) weight += smell[2][i];
				break;
			case RIGHT: for (int i = 0; i < smell.length; i++) weight += smell[i][2];
				break;
			case LEFT: for (int i = 0; i < smell.length; i++) weight += smell[i][0];
				break;
			default:
				break;
		}

		return weight*(-1);
	}

	private double calcVisionWeight(State s) {
		double weight = 0;

		int[][] actionMatrix = Util.cutVision(vision, s.getAction());

		Point position = null;

		switch(s.getAction()) {
			case UP:
				position = new Point(2, 1);
				break;

			case DOWN:
				position = new Point(2, 0);
				break;

			case RIGHT:
				position = new Point(0, 2);
				break;

			case LEFT:
				position = new Point(1, 2);
				break;
			default:
				break;
		}

		for(int i =0; i < actionMatrix.length; i++) {
			for(int j = 0; j < actionMatrix[i].length; j++) {
				int cell = actionMatrix[i][j];

				if(cell == 0)
					continue;

				double distance = Util.getDistance(position, j, i);

				if(distance == 0) {
					weight += identifyWeightByCode(cell);
				}else {
					weight += identifyWeightByCode(cell) / distance;
				}
			}
		}

		return weight;
	}

	private float identifyWeightByCode(int cell) {
		if(cell == 0)
			return 0;
		EMapCode mapObject = EMapCode.fromValue(cell);
		if(mapObject == null)
			System.out.println(cell);

		switch (mapObject) {
			case FLOOR:
				return EGameObjectWeight.FLOOR.getValue();
			case UNKNOW_CELL:
				return EGameObjectWeight.UNKNOW_CELL.getValue();
			case NO_VISION:
				return EGameObjectWeight.NO_VISION.getValue();
			case OUT_MAP:
				return EGameObjectWeight.OUT_MAP.getValue();
			case WALL:
				return EGameObjectWeight.WALL.getValue();
			case BANK:
				if(haveMoney())
					return EGameObjectWeight.BANK.getValue();
				break;
			case COIN:
				return EGameObjectWeight.COIN.getValue();
			case POWER_UP:
				if(shouldIBuyThePowerUp())
					return EGameObjectWeight.POWER_UP.getValue();
				break;
			case SAVER:
				return EGameObjectWeight.SAVER.getValue();
			case THIEF:
				if (sensor.getNumeroJogadasImunes() == 0)
					return EGameObjectWeight.THIEF.getValue();
				return EGameObjectWeight.THIEF.getValue() / sensor.getNumeroJogadasImunes();
			default:
				return 0;
		}
		return 0;
	}
}

class State{
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

class Util {
	public static double getDistance(Point a, int x, int y) {
		return getDistance(a, new Point(x,y));
	}

	public static double getDistance(Point a, Point b) {
		return Math.abs(b.x - a.x) + Math.abs(b.y - a.y); //Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
	}

	public static SubMatrix cutMatrix(int[][] matrix, Point currentPosition, EAction action){
		int mMatrix = matrix.length;
		int nMatrix = matrix[0].length;
		int m = 0;
		int n = 0;
		int i = 0;
		int j =  0;

//		currentPosition
		if(action.getValue() == EAction.UP.getValue() || EAction.DOWN.getValue() == action.getValue()) {
			n = nMatrix;

			if(action.getValue() == EAction.UP.getValue()) {
				m = currentPosition.y;
			}else {
				m = mMatrix - currentPosition.y;
				i = currentPosition.y + 1;
			}
		}else {
			m = mMatrix;

			if(action.getValue() == EAction.LEFT.getValue()) {
				n = currentPosition.x;
			}else {
				n = nMatrix - currentPosition.x;
				j = currentPosition.x + 1;
			}
		}

		return new SubMatrix(m, n, i, j);
	}

	public static int[][] cutVision(int [][] vision, EAction action){
		int m = (action.getValue() == EAction.UP.getValue() || EAction.DOWN.getValue() == action.getValue()) ? 2 : 5;
		int n = (m == 5) ? 2 : 5;

		int i = action.getValue() == EAction.DOWN.getValue() ? 3 : 0;
		int j = action.getValue() == EAction.RIGHT.getValue() ? 3 : 0;

		return cutMatrix(vision, m, n, i ,j);
	}

	private static int[][] cutMatrix(int[][] matrix, int m, int n, int i, int j){
		int [][] newMatrix = new int [m][n];

		for(int count_i = 0; count_i < m; count_i++, i++) {
			for(int count_j = 0, variable_j = j; count_j < n; count_j++, variable_j++) {
				newMatrix[count_i][count_j] = matrix[i][variable_j];
			}
		}

		return newMatrix;
	}

	public static boolean isInMap(int [][] map, Point point) {
		return isInMap(map, point.y, point.x);
	}

	public static boolean isInMap(int [][] map, int y, int x) {
		return map.length > y && map[0].length > x &&
				y >= 0 && x >= 0;
	}

	public static boolean isWalkable(int [][] map, Point point, boolean payment, boolean haveMoney) {
		return isWalkable(map, point.y, point.x, payment, haveMoney);
	}

	public static boolean isWalkable(int [][] map, int y, int x, boolean payment, boolean haveMoney) {
		int cell = map[y][x];

		return 	cell == EMapCode.FLOOR.getValue() 	 ||
				cell == EMapCode.COIN.getValue() 	 ||
				(payment && cell == EMapCode.POWER_UP.getValue()) ||
				(haveMoney && cell == EMapCode.BANK.getValue());
	}

	public static int[][] getSensorArrayAsMatrix(int[] array, int m, int centerValue) {
		int n = (array.length + 1) / m;
		int[][] matrix = new int[m][n];

		int i_center = (int) Math.ceil(m / 2.0) - 1;
		int j_center = (int) Math.ceil(n / 2.0) - 1;

		int offset = 0;
		for (int i = 0; i < m; i++) {

			for (int j = 0; j < n; j++) {
				if (i_center == i && j_center == j) {
					offset = 1;
					matrix[i][j] = centerValue;
					continue;
				}

				matrix[i][j] = array[i * n + j - offset];
			}
		}

		return matrix;
	}
}

class SubMatrix {
	public int m;
	public int n;
	public int i;
	public int j;

	public int[][] matrix;

	public SubMatrix(int m, int n, int i, int j) {
		this.m = m;
		this.n = n;
		this.i = i;
		this.j = j;
	}
}

enum EAction{
	STOP(0), UP(1), DOWN(2), RIGHT(3), LEFT(4);

	private int value;

	EAction(int value){
		this.value = value;
	}

	public static EAction fromValue(int id) {
		for(EAction e : EAction.values()){
			if(e.value == id){
				return e;
			}
		}

		return null;
	}

	public int getValue() {
		return value;
	}
}

enum EGameObjectWeight{
	COIN			(40),
	POWER_UP		(50),
	THIEF			(-400),
	SAVER			(0),
	BANK			(400),
	OUT_MAP			(0),
	NO_VISION		(0),
	UNKNOW_CELL		(5),
	WALL			(0),
	FLOOR           (0);

	private float value;

	EGameObjectWeight(float value){
		this.value = value;
	}

	public static EGameObjectWeight fromValue(int id) {
		for(EGameObjectWeight e : EGameObjectWeight.values()){
			if(e.value == id){
				return e;
			}
		}

		return null;
	}

	public float getValue() {
		return value;
	}
}

enum EMapCode{
	UNKNOW_CELL		(-5),
	NO_VISION		(-2),
	OUT_MAP			(-1),
	FLOOR			(0),
	WALL			(1),
	SELF_POSITION	(2),
	BANK			(3),
	COIN			(4),
	POWER_UP		(5),
	SAVER			(100),
	THIEF			(200);

	private int value;

	EMapCode(int value){
		this.value = value;
	}

	public static EMapCode fromValue(int id) {
		if (id >= THIEF.getValue())
			return EMapCode.THIEF;
		if (id >= SAVER.getValue())
			return EMapCode.SAVER;

		for(EMapCode e : EMapCode.values()){
			if(e.value == id){
				return e;
			}
		}

		return null;
	}

	public int getValue() {
		return value;
	}
}
