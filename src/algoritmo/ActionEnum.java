package algoritmo;

public enum ActionEnum {
    STOP(0), UP(1), DOWN(2), RIGHT(3), LEFT(4);

    private int value;

    ActionEnum(int value){
        this.value = value;
    }

    public static ActionEnum fromValue(int id) {
        for(ActionEnum e : ActionEnum.values()){
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
