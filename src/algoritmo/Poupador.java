package algoritmo;

import model.*;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Poupador extends ProgramaPoupador {
    private boolean firstIteration = true;
    private Point currentPosition;

    private MapEnum[][] map;
    private int[][] discovered;
    private MapEnum[][] vision;
    private int[][] smell;
    private boolean chasedByThief;

    @Override
    public int acao() {
        initVariables();
        updateVariables();

        List<StateToGo> newPossibleStates = getStateSuccessors(this.currentPosition);

        for(StateToGo s : newPossibleStates)
            s.setWeight(getStateWeight(s));

        Collections.shuffle(newPossibleStates);
        Collections.sort(newPossibleStates, StateToGo.comparator());

        if(!newPossibleStates.isEmpty()) {
            StateToGo value = newPossibleStates.get(newPossibleStates.size() - 1);
            //if (map[value.getPosition().y][value.getPosition().x] == EMapCode.POWER_UP.getValue())
              //  attemptsToPickUpPower = 0;

            return value.getAction().getValue();
        }

        return ActionEnum.STOP.getValue();
    }

    private void initVariables() {
        if (firstIteration) {
            map = new MapEnum[GlobalVariables.MAP_WIDTH][GlobalVariables.MAP_HEIGHT];
            discovered = new int[GlobalVariables.MAP_WIDTH][GlobalVariables.MAP_HEIGHT];

            for (int i = 0; i < GlobalVariables.MAP_WIDTH; i++)
                for (int j = 0; j < GlobalVariables.MAP_HEIGHT; j++){
                    map[i][j] = MapEnum.UNKNOW_CELL;
                    discovered[i][j] = MapEnum.UNKNOW_CELL.getValue();
                }

            firstIteration = false;
        }
    }

    private void updateVariables() {
        currentPosition = sensor.getPosicao();
        discovered[currentPosition.x][currentPosition.y] += 1;
        vision = getVisionArrayAsMatrix(sensor.getVisaoIdentificacao(), GlobalVariables.VISION_MATRIX_SIZE, MapEnum.SELF_POSITION.getValue());
        smell = getSensorArrayAsMatrix(sensor.getAmbienteOlfatoLadrao(), GlobalVariables.SMELL_MATRIX_SIZE, MapEnum.SELF_POSITION.getValue());
        chasedByThief = chasedByThief();
        updateMap();
    }

    private void updateMap() {
        int offset = GlobalVariables.VISION_MATRIX_SIZE/2;
        int m, n = m = GlobalVariables.VISION_MATRIX_SIZE;

        for (int i = this.currentPosition.y - offset, count_i = 0; count_i < m; count_i++, i++) {
            for (int j = this.currentPosition.x - offset, count_j = 0; count_j < n; count_j++, j++) {
                MapEnum cellValue = vision[count_i][count_j];

                if (isInMap(new Point(j, i)) && cellValue != MapEnum.NO_VISION) {
                    this.map[j][i] = cellValue;
                    /*if (cellValue == MapEnum.BANK){
                        PoupadorX.BANK_X = i;
                        PoupadorX.BANK_Y = j;
                    }*/
                }
            }
        }
    }

    private static MapEnum[][] getVisionArrayAsMatrix(int[] array, int m, int centerValue) {
        int n = (array.length + 1) / m;
        MapEnum[][] matrix = new MapEnum[m][n];

        int i_center = (int) Math.ceil(m / 2.0) - 1;
        int j_center = (int) Math.ceil(n / 2.0) - 1;

        int offset = 0;
        for (int i = 0; i < m; i++) {

            for (int j = 0; j < n; j++) {
                if (i_center == i && j_center == j) {
                    offset = 1;
                    matrix[i][j] = MapEnum.fromValue(centerValue);
                    continue;
                }

                matrix[i][j] = MapEnum.fromValue(array[i * n + j - offset]);
            }
        }

        return matrix;
    }

    private static int[][] getSensorArrayAsMatrix(int[] array, int m, int centerValue) {
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

    private boolean chasedByThief() {
        for (int i = 0; i < vision.length; i++){
            for (int j = 0; j < vision[i].length; j++) {
                if (vision[i][j] == MapEnum.THIEF)
                    return true;
            }
        }

        return false;
    }

    private List<StateToGo> getStateSuccessors(Point point) {
        List<StateToGo> validStates = new ArrayList<StateToGo>();

        StateToGo[] nextStates = {
                new StateToGo(ActionEnum.UP, new Point(point.x, point.y - 1)),
                new StateToGo(ActionEnum.DOWN, new Point(point.x, point.y + 1)),
                new StateToGo(ActionEnum.RIGHT, new Point(point.x + 1, point.y)),
                new StateToGo(ActionEnum.LEFT, new Point(point.x - 1, point.y))
        };

        for(StateToGo s : nextStates) {
            if(isInMap(s.getPosition())) {
                if (canWalkTo(s.getPosition()))
                    validStates.add(s);
                //if(map[y][x] == EMapCode.POWER_UP.getValue())
                  //  attemptsToPickUpPower++;
            }
        }

        return validStates;
    }

    private boolean isInMap(Point current) {
        return map.length > current.x && map[0].length > current.y &&
                current.y >= 0 && current.x >= 0;
    }

    private boolean canWalkTo(Point current) {
        MapEnum cell = map[current.x][current.y];

        return cell == MapEnum.FLOOR ||
               cell == MapEnum.COIN ||
                (shouldIBuyThePowerUp() && cell == MapEnum.POWER_UP) ||
                (haveMoney() && cell == MapEnum.BANK);
    }

    private boolean shouldIBuyThePowerUp() { return chasedByThief && sensor.getNumeroDeMoedas() > GlobalVariables.POWER_UP; }
    private boolean haveMoney() { return sensor.getNumeroDeMoedas() > 0; }

    private double getStateWeight(StateToGo s) {
        double weight = 0;

        weight = calcVisionWeight(s);
        weight += calcSmellWeight(s);
        weight += calcExplorationWeight(s);

        return weight;
    }

    private double calcVisionWeight(StateToGo s) {
        double weight = 0;

        MapEnum[][] actionMatrix = cutVision(s);

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
                MapEnum cell = actionMatrix[i][j];

                if(cell == MapEnum.FLOOR)
                    continue;

                double distance = Util.getDistance(position, j, i);

                if(distance == 0)
                    weight += identifyWeightByCode(cell);
                else
                    weight += identifyWeightByCode(cell) / distance;
            }
        }

        return weight;
    }

    private double calcSmellWeight(StateToGo s) {
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



    private double calcExplorationWeight(StateToGo s) {
        double weight = 0;

        SubMatrix submatrix = cutMatrix(map, currentPosition, s.getAction());
        printMatrix(map);

        for(int i = submatrix.i; i < submatrix.n; i++) {
            for(int j = submatrix.j; j < submatrix.m; j++) {
                MapEnum cellMap = map[i][j];

                if(cellMap != MapEnum.FLOOR) {
                    double distance = Util.getDistance(s.getPosition(), j, i);
                    double iterationWeight = identifyWeightByCode(cellMap);

                    if(distance == 0)
                        weight += iterationWeight ;
                    else
                        weight += iterationWeight  / distance;
                }
            }
        }

        weight -= discovered[s.getPosition().x][s.getPosition().y];

        return weight;
    }

    private void printMatrix(MapEnum[][] matrix) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("matriz.txt"));


            for (int i = 0; i < matrix.length; i++)  {
                for (int j = 0; j < matrix[0].length; j++)     {
                    String str = padRight(matrix[j][i].toString(), 15);
                    System.out.print(str);
                    writer.append(str);
                }
                System.out.println(" "); //muda de linha
                writer.append(" \n");
            }
            writer.append(" \n");
            writer.append(" \n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static SubMatrix cutMatrix(MapEnum[][] matrix, Point currentPosition, ActionEnum action){
        int mMatrix = matrix.length;
        int nMatrix = matrix[0].length;
        int m = 0;
        int n = 0;
        int i = 0;
        int j =  0;
        switch (action){
            case UP:
                i = 0;
                j = 0;
                n = nMatrix;
                m = currentPosition.y;
                break;
            case DOWN:
                i = 0;
                j = currentPosition.y + 1;
                n = nMatrix;
                m = mMatrix - currentPosition.y;
                break;
            case RIGHT:
                i = currentPosition.x + 1;
                j = 0;
                n = nMatrix - currentPosition.x;
                m = mMatrix;
                break;
            case LEFT:
                i = 0;
                j = 0;
                n = currentPosition.x;
                m = mMatrix;
                break;
        }

        return new SubMatrix(m, n, i, j);
    }

    public MapEnum[][] cutVision(StateToGo s){
        SubMatrix c = cutMatrix(vision, s.getPosition(), s.getAction());

        MapEnum[][] aux = new MapEnum[c.n][c.m];
        for (int i = 0; i < c.n; i++) {
            for (int j = 0; j < c.m; j++) {
                aux[i][j] = vision[i+c.i][j+c.j];
            }
        }
        return aux;
        /*int m = (action.getValue() == ActionEnum.UP.getValue() || ActionEnum.DOWN.getValue() == action.getValue()) ? 2 : 5;
        int n = (m == 5) ? 2 : 5;

        int i = action.getValue() == ActionEnum.DOWN.getValue() ? 3 : 0;
        int j = action.getValue() == ActionEnum.RIGHT.getValue() ? 3 : 0;

        return cutMatrix(vision, m, n, i ,j);*/
    }

    private static MapEnum[][] cutMatrix(MapEnum[][] matrix, int m, int n, int i, int j){
        MapEnum [][] newMatrix = new MapEnum [m][n];

        for(int count_i = 0; count_i < m; count_i++, i++) {
            for(int count_j = 0, variable_j = j; count_j < n; count_j++, variable_j++) {
                newMatrix[count_i][count_j] = matrix[i][variable_j];
            }
        }

        return newMatrix;
    }

    private float identifyWeightByCode(MapEnum cell) {
        if(cell == MapEnum.FLOOR)
            return 0;
        if(cell == null)
            System.out.println(cell);

        switch (cell) {
            case FLOOR:
                return MapValuesEnum.FLOOR.getValue();
            case UNKNOW_CELL:
                return MapValuesEnum.UNKNOW_CELL.getValue();
            case NO_VISION:
                return MapValuesEnum.NO_VISION.getValue();
            case OUT_MAP:
                return MapValuesEnum.OUT_MAP.getValue();
            case WALL:
                return MapValuesEnum.WALL.getValue();
            case BANK:
                if(haveMoney())
                    return MapValuesEnum.BANK.getValue();
                break;
            case COIN:
                return MapValuesEnum.COIN.getValue();
            case POWER_UP:
                if(shouldIBuyThePowerUp())
                    return MapValuesEnum.POWER_UP.getValue();
                break;
            case SAVER:
                return MapValuesEnum.SAVER.getValue();
            case THIEF:
                if (sensor.getNumeroJogadasImunes() == 0)
                    return MapValuesEnum.THIEF.getValue();
                return MapValuesEnum.THIEF.getValue() / sensor.getNumeroJogadasImunes();
            default:
                return 0;
        }
        return 0;
    }
}
