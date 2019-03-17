package algoritmo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import algoritmo.poupador.State;
import algoritmo.poupador.SubMatrix;
import algoritmo.poupador.Util;
import algoritmo.poupador.enums.EAction;
import algoritmo.poupador.enums.EGameObjectWeight;
import algoritmo.poupador.enums.EMapCode;

public class PoupadorDaniel extends ProgramaPoupador {
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
	private static Point bankPosition = new Point(8,8);
	
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
		map[bankPosition.y][bankPosition.x] = EMapCode.BANK.getValue();
	}

	public int acao() {
		
		updateMap();
		
		List<State> newPossibleStates = getStateSuccessors(this.currentPosition);
		
		for(State s : newPossibleStates) {
			s.setWeight(getStateWeight(s));
		}
		
		Collections.shuffle(newPossibleStates);
		
		Collections.sort(newPossibleStates, State.comparator());
		
		if(!newPossibleStates.isEmpty()) {
			return newPossibleStates.get(newPossibleStates.size() - 1).getAction().getValue();
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
		if (firstIteration) {
			instanciation();			
		} else {
			clearAgentsFromMap(currentPosition);
		}

		agentsMap = new Hashtable<Integer, Point>();

		currentPosition = sensor.getPosicao();
		walkMemory[currentPosition.y][currentPosition.x] += 1;
		
		vision = Util.getSensorArrayAsMatrix(sensor.getVisaoIdentificacao(), VISION_MATRIX_SIZE, EMapCode.SELF_POSITION.getValue());
		smell = Util.getSensorArrayAsMatrix(sensor.getAmbienteOlfatoLadrao(), SMELL_MATRIX_SIZE, EMapCode.SELF_POSITION.getValue());
		
		money = sensor.getNumeroDeMoedas();
		
		fillVisualMap(vision);
		
		
		escaping = seeingThief();
	}
	
	private boolean seeingThief() {
		for (Entry<Integer, Point> tuple : agentsMap.entrySet()) {
			Integer agentKey = tuple.getKey();
			
			if(agentKey >= EMapCode.THIEF.getValue()) {
				return true;
			}
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
		
		if(oldPosition != null) {
			this.map[oldPosition.y][oldPosition.x] = 0;			
		}
	}
	
	private boolean shouldIBuyThePowerUp() {
		return escaping && money > POWER_UP_PRICE && sensor.getNumeroJogadasImunes() == 0;
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
			if(Util.isInMap(map, s.getPosition())) {
				if(Util.isWalkable(map, s.getPosition(), shouldIBuyThePowerUp(), haveMoney())) {
					validStates.add(s);
				}
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
//				int cellWalkMemory = walkMemory[i][j];

				if(cellMap != 0) {
					double distance = Util.getDistance(s.getPosition(), j, i);
					
					double iterationWeight = identifyWeightByCode(cellMap) / 10;
					
					if(distance == 0) {
						weight += iterationWeight ;
					}else {
						weight += iterationWeight  / distance;
					}
				}
				
//				weight -= cellWalkMemory;
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
				
				if(cell == 0) continue;
				
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
		if(cell == 0) return 0;
		
		if(cell > EMapCode.THIEF.getValue()){
			cell = EMapCode.THIEF.getValue();
		}else if(cell > EMapCode.SAVER.getValue()) {
			cell = EMapCode.SAVER.getValue();
		}
		
		EMapCode mapObject = EMapCode.fromValue(cell);
		
		if(mapObject == null) System.out.println(cell);
		
		if(mapObject.getValue() == EMapCode.BANK.getValue()) {
			if(haveMoney()) {
				return EGameObjectWeight.BANK.getValue();				
			}
			
		}else if(mapObject.getValue() == EMapCode.COIN.getValue()) {
			return EGameObjectWeight.COIN.getValue();
			
		}else if(mapObject.getValue() == EMapCode.NO_VISION.getValue()) {
			return EGameObjectWeight.NO_VISION.getValue();
			
		}else if(mapObject.getValue() == EMapCode.OUT_MAP.getValue()) {
			return EGameObjectWeight.OUT_MAP.getValue();
			
		}else if(mapObject.getValue() == EMapCode.POWER_UP.getValue()) {
			if(shouldIBuyThePowerUp()) {
				return EGameObjectWeight.POWER_UP.getValue();				
			}
			
		}else if(mapObject.getValue() == EMapCode.THIEF.getValue()) {
			return EGameObjectWeight.THIEF.getValue();
			
		}else if(mapObject.getValue() == EMapCode.WALL.getValue()) {
			return EGameObjectWeight.WALL.getValue();
			
		}else if(mapObject.getValue() == EMapCode.UNKNOW_CELL.getValue()) {
			return EGameObjectWeight.UNKNOW_CELL.getValue();
			
		}else if(mapObject.getValue() == EMapCode.SAVER.getValue()){
		}else {
			System.out.println("Unidentified code: "+cell);
			
		}
		
		return 0;
	}
}



















