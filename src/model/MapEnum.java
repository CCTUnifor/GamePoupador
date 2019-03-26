package model;

public enum MapEnum {
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

    MapEnum(int value){
        this.value = value;
    }

    public static MapEnum fromValue(int id) {
        if (id >= THIEF.getValue())
            return MapEnum.THIEF;
        if (id >= SAVER.getValue())
            return MapEnum.SAVER;

        for(MapEnum e : MapEnum.values()){
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
