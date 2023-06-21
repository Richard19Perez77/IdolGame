package sprite.pixel.canvas.movables;

public class Bullet {
	public int x,y;
	public int fireSpeed;
	public boolean exists;
	public int center;
	
	public void moveFire(){
		center = x + 20;
		y -= fireSpeed;
	}
	
}
