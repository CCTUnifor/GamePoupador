package algoritmo.poupador.enums;

public enum EGameObjectWeight{
	COIN			(40), 
	POWER_UP		(50), 
	THIEF			(-400), 
	SAVER			(0), 
	BANK			(400),
	OUT_MAP			(0),  
	NO_VISION		(0), 
	UNKNOW_CELL		(1), 
	WALL			(0);
	
	
	private float value;
	
	EGameObjectWeight(float value){
		this.value = value;
	}
	
	public static EGameObjectWeight fromValue(int id) {
		for(EGameObjectWeight e : EGameObjectWeight.values()){
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
