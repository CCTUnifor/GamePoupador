package algoritmo.poupador.enums;

public enum EMapCode{
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
	
	EMapCode(int value){
		this.value = value;
	}
	
	public static EMapCode fromValue(int id) {
		for(EMapCode e : EMapCode.values()){
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
