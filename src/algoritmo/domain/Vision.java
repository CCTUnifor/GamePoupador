package algoritmo.domain;

import algoritmo.ActionEnum;
import algoritmo.SensoresPoupador;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Vision {
    private VisionEnum[] vision;
    private SmellEnum[] visionSmell;
    private SensoresPoupador sensor;
    private static final int INDEX_TOP = 7;
    private static final int INDEX_BOTTOM = 16;
    private static final int INDEX_LEFT = 11;
    private static final int INDEX_RIGHT = 12;

    private static final int INDEX_TOP_SMELL = 1;
    private static final int INDEX_BOTTOM_SMELL = 6;
    private static final int INDEX_LEFT_SMELL = 3;
    private static final int INDEX_RIGHT_SMELL = 4;


    public Vision(SensoresPoupador sensor) {
        this.sensor = sensor;
        this.vision = new VisionEnum[(GlobalVariables.VISION_MATRIX_SIZE * GlobalVariables.VISION_MATRIX_SIZE) - 1];
        this.visionSmell = new SmellEnum[(GlobalVariables.SMELL_MATRIX_SIZE * GlobalVariables.SMELL_MATRIX_SIZE) - 1];
    }

    public VisionEnum[] getVision() {
        return vision;
    }

    public SmellEnum[] getVisionSmell() {
        return visionSmell;
    }

    public VisionEnum getTop() { return vision[INDEX_TOP]; }
    public VisionEnum getBottom() { return vision[INDEX_BOTTOM]; }
    public VisionEnum getLeft() { return vision[INDEX_LEFT]; }
    public VisionEnum getRight() { return vision[INDEX_RIGHT]; }

    public SmellEnum getTopSmell() { return visionSmell[INDEX_TOP_SMELL]; }
    public SmellEnum getBottomSmell() { return visionSmell[INDEX_BOTTOM_SMELL]; }
    public SmellEnum getLeftSmell() { return visionSmell[INDEX_LEFT_SMELL]; }
    public SmellEnum getRightSmell() { return visionSmell[INDEX_RIGHT_SMELL]; }

    public void setSensor(SensoresPoupador sensor) { this.sensor = sensor; }

    public void update() {
        updateVision();
        updateVisionSmell();
    }

    private void updateVision() {
        int[] id =  this.sensor.getVisaoIdentificacao();
        for (int i = 0; i < id.length; i++) {
            int value = id[i];
            vision[i] = VisionEnum.fromValue(value);
        }
    }

    private void updateVisionSmell() {
        int[] id =  this.sensor.getAmbienteOlfatoPoupador();
        for (int i = 0; i < id.length; i++) {
            int value = id[i];
            visionSmell[i] = SmellEnum.fromValue(value);
        }
    }

    public List<ActionEnum> getCoinCells() {
        List<ActionEnum> posibleActions = new ArrayList<ActionEnum>();
        List<Integer> positions = getCoinsPosition();

        for (int i = 0; i < positions.size(); i++) {
            int position = positions.get(i);
            if (ehQuadranteUm(position))
                posibleActions.add(ActionEnum.UP);
            if (ehQuadranteDois(position))
                posibleActions.add(ActionEnum.LEFT);
            if (ehQuadranteTres(position))
                posibleActions.add(ActionEnum.DOWN);
            if (ehQuadranteQuatro(position))
                posibleActions.add(ActionEnum.RIGHT);
        }

        return posibleActions;
    }

    public ActionEnum getQuadrante(Point p) {
        Point saverPosition = sensor.getPosicao();
        if (saverPosition.x - p.x < 0)
            return ActionEnum.LEFT;
        if (saverPosition.x - p.x > 0)
            return ActionEnum.RIGHT;
        if (saverPosition.y - p.y < 0)
            return ActionEnum.DOWN;
        if (saverPosition.y - p.y > 0)
            return ActionEnum.UP;
        return ActionEnum.STOP;
    }

    private List<Integer> getCoinsPosition() {
        List<Integer> positions = new ArrayList<Integer>();
        for (int i = 0; i < this.vision.length; i++) {
            if (this.vision[i] == VisionEnum.COIN)
                positions.add(i);
        }
        return positions;
    }

    private boolean ehQuadranteUm(int position) {
        List<Integer> quadrante = Arrays.asList( 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 );
        return quadrante.contains(position);
    }

    private boolean ehQuadranteDois(int position) {
        List<Integer> quadrante = Arrays.asList( 0, 1, 5, 6, 10, 11, 15, 16, 20, 21 );
        return quadrante.contains(position);
    }

    private boolean ehQuadranteTres(int position) {
        List<Integer> quadrante = Arrays.asList( 15, 16, 17, 18, 19, 20, 21, 21, 23, 24 );
        return quadrante.contains(position);
    }

    private boolean ehQuadranteQuatro(int position) {
        List<Integer> quadrante = Arrays.asList( 3, 4, 8, 9, 13, 14, 18, 19, 23, 24 );
        return quadrante.contains(position);
    }
}