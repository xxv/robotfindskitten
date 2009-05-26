package info.staticfree.android.robotfindskitten;

public class Thing {
	public enum ThingType {ROBOT, KITTEN, NKI};
	
	public int x;
	public int y;
	public String message;
	public ThingType type;
	public Object representation;
	
	public Thing(ThingType type){
		this.type = type;
	}
	
	public void setLocation(int x, int y){
		this.x = x;
		this.y = y;
	}
}
