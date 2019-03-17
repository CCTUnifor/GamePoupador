package algoritmo.domain;

import algoritmo.SensoresPoupador;

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
}