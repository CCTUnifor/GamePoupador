package algoritmo.domain;

public enum SmellEnum {
    NO_MARK			(0),
    ONE_MARK		(1),
    TWO_MARK     	(2),
    THREE_MARK		(3),
    FOUR_MARK		(4),
    FIVE_MARK 		(5);

    private int value;

    SmellEnum(int value){
        this.value = value;
    }

    public static SmellEnum fromValue(int id) {
        for(SmellEnum e : SmellEnum.values()){
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
