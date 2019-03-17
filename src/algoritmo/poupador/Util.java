package algoritmo.poupador;

import java.awt.Point;

import algoritmo.poupador.enums.EAction;
import algoritmo.poupador.enums.EMapCode;

public class Util {	
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
		
		// for up, left, right i starts in 0
		int i = action.getValue() == EAction.DOWN.getValue() ? 3 : 0;
		// for up, left, down j starts in 0
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
	
	public static EAction getDecisionAction(Point agent, Point obj) {
		if(agent.getY() > obj.getY()){
			return EAction.UP;
		}
		else {
			if(agent.getY() < obj.getY()) {
				return EAction.DOWN;
			}
			else {
				if(agent.getX() > obj.getX()) {
					return EAction.LEFT;
				}
				else {
					return EAction.RIGHT;
				}
			}
		}
		
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
	
	public static void printMatrix(int[][] array) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				System.out.printf("%5d", array[i][j]);
			}
			System.out.println();
		}
	}

	public static void printSensorArrayAsMatrix(int[] array, int m) {
		int n = (array.length + 1) / m;

		int i_center = (int) Math.ceil(m / 2.0) - 1;
		int j_center = (int) Math.ceil(n / 2.0) - 1;

		int offset = 0;
		for (int i = 0; i < m; i++) {

			for (int j = 0; j < n; j++) {
				if (i_center == i && j_center == j) {
					offset = 1;
					System.out.printf("     ");
					continue;
				}

				System.out.printf("%5d", array[i * n + j - offset]);// i*n+j);
			}

			System.out.println();
		}
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
