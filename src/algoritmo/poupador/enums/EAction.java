package algoritmo.poupador.enums;

public enum EAction{
	STOP(0), UP(1), DOWN(2), RIGHT(3), LEFT(4);
	
	private int value;
	
	EAction(int value){
		this.value = value;
	}
	
	public static EAction fromValue(int id) {
		for(EAction e : EAction.values()){
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
