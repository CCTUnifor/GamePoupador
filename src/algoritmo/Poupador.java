package algoritmo;

import model.*;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Poupador extends ProgramaPoupador {
    private boolean firstIteration = true;
    private Point currentPosition;

    private MapEnum[][] map;
    private int[][] discovered;
    private MapEnum[] vision;
    private int[] smell;
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
        vision = getVisionArrayAsMatrix(sensor.getVisaoIdentificacao());
        smell = sensor.getAmbienteOlfatoLadrao();
        chasedByThief = chasedByThief();
        updateMap();
    }

    private void updateMap() {
        int currentX = currentPosition.x;
        int currentY = currentPosition.y;

        for (int i = 0; i < vision.length; i++) {
            MapEnum cellValue = vision[i];
            int x = currentX + getLine(i);
            int y = currentY + getColumn(i);

            if (isInMap(new Point(x, y)))
                map[x][y] = cellValue;
        }
        map[currentX][currentY] = MapEnum.SELF_POSITION;

        printMatrix(map);
    }

    private int getLine(int i) {
        if (i >= 0 && i <= 4)
            return -2;
        if (i >= 5 && i <= 9)
            return -1;
        if (i >= 10 && i <= 14)
            return 0;
        if (i >= 15 && i <= 19)
            return 1;
        if (i >= 20 && i < 24)
            return 2;
        return 0;
    }

    private int getColumn(int i) {
        switch (i){
            case 0:
            case 5:
            case 10:
            case 15:
            case 20:
                return -2;
            case 1:
            case 6:
            case 11:
            case 16:
            case 21:
                return -1;
            case 2:
            case 7:
            case 17:
            case 22:
                return 0;
            case 3:
            case 8:
            case 12:
            case 18:
            case 23:
                return 1;
            case 4:
            case 9:
            case 13:
            case 19:
                return 2;
        }
        return 0;
    }

    private static MapEnum[] getVisionArrayAsMatrix(int[] array) {
        MapEnum[] matrix = new MapEnum[array.length];
        for (int i = 0; i < matrix.length; i++)
            matrix[i] = MapEnum.fromValue(array[i]);
        return matrix;
    }

    private boolean chasedByThief() {
        for (int i = 0; i < vision.length; i++){
            if (vision[i] == MapEnum.THIEF)
                return true;
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

        //weight = calcVisionWeight(s);
        weight += calcSmellWeight(s);
        weight += calcExplorationWeight(s);

        return weight;
    }

    private double calcSmellWeight(StateToGo s) {
        float weight = 0;

        switch(s.getAction()) {
            case UP: for (int i = 0; i < 3; i++) weight += smell[i];
                break;
            case DOWN: for (int i = 5; i < 8; i++) weight += smell[i];
                break;
            case RIGHT:
                weight += smell[2] + smell[4] + smell[7];
                break;
            case LEFT:
                weight += smell[0] + smell[3] + smell[5];
                break;
            default:
                break;
        }

        return weight*(-1);
    }

    private double calcExplorationWeight(StateToGo s) {
        double weight = 0;

        SubMatrix submatrix = cutMatrix(map, currentPosition, s.getAction());

        for(int i = submatrix.i; i < submatrix.n; i++) {
            for(int j = submatrix.j; j < submatrix.m; j++) {
                MapEnum cellMap = map[i][j];

                if(cellMap != MapEnum.FLOOR) {
                    //double distance = Util.getDistance(s.getPosition(), j, i);
                    double iterationWeight = identifyWeightByCode(cellMap);

                    //if(distance == 0)
                        weight += iterationWeight ;
                    //else
                        //weight += iterationWeight  / distance;
                }
            }
        }

        weight -= discovered[s.getPosition().x][s.getPosition().y];

        return weight;
    }

    private void printMatrix(MapEnum[][] matrix) {
        BufferedWriter writer = null;
        try {
            SimpleDateFormat d = new SimpleDateFormat("yyyymmdd hhmmss");
            writer = new BufferedWriter(new FileWriter("matriz"+d.format(new Date())+".txt"));


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
/*
    public MapEnum[][] cutVision(StateToGo s){
        SubMatrix c = cutMatrix(vision, s.getPosition(), s.getAction());

        MapEnum[][] aux = new MapEnum[c.n][c.m];
        for (int i = 0; i < c.n; i++) {
            for (int j = 0; j < c.m; j++) {
                aux[i][j] = vision[i+c.i][j+c.j];
            }
        }
        return aux;
        //int m = (action.getValue() == ActionEnum.UP.getValue() || ActionEnum.DOWN.getValue() == action.getValue()) ? 2 : 5;
        //int n = (m == 5) ? 2 : 5;

        //int i = action.getValue() == ActionEnum.DOWN.getValue() ? 3 : 0;
        //int j = action.getValue() == ActionEnum.RIGHT.getValue() ? 3 : 0;

        //return cutMatrix(vision, m, n, i ,j);
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
*/
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
