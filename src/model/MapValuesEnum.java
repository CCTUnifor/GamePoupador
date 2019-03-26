package model;

public enum MapValuesEnum {
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

    MapValuesEnum(float value){
        this.value = value;
    }

    public static MapValuesEnum fromValue(int id) {
        for(MapValuesEnum e : MapValuesEnum.values()){
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
