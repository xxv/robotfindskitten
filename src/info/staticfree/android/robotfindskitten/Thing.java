package info.staticfree.android.robotfindskitten;

public class Thing {
	public enum ThingType {ROBOT, KITTEN, NKI};
	
	public int x = -1;
	public int y = -1;
	public String message;
	public ThingType type;
	public String character;
	public int color;
	
	public Thing(ThingType type){
		this.type = type;
		if (type == ThingType.ROBOT){
			character = "#";
		}
	}
	
	public void setLocation(int x, int y){
		this.x = x;
		this.y = y;
	}
}
