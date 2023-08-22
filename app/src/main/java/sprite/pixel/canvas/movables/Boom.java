package sprite.pixel.canvas.movables;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Boom {

	public Bitmap[] booms;
	public int x;
	public int y;
	public int frames;
	public int currentFrame;
	public int fps;
	public int counter;
	public boolean booming;
	public boolean crit = false;
	public int critx, crity;
	public Paint paint;
	public String c;

	public Boom(){

	}

    public Boom(Bitmap[] b) {
		booms = b;
		frames = b.length;
		fps = 4;
		currentFrame = 0;
		counter = 0;
		booming = false;
		c = "Critical Hit!";
		paint = new Paint();
		paint.setColor(Color.RED);
		paint.setTextSize(35);
		paint.setStrokeWidth(4);
	}

	public void update(Canvas canvas) {

		if (currentFrame < frames) {
			canvas.drawBitmap(booms[currentFrame], x, y, null);
		}

		if (crit) {
			if (paint.getColor() == Color.BLUE) {
				paint.setColor(Color.RED);
			} else if (paint.getColor() == Color.RED) {
				paint.setColor(Color.WHITE);
			} else {
				paint.setColor(Color.BLUE);
			}
			canvas.drawText(c, critx, crity, paint);
			crity += 2;
		}

		if (counter > 40) {
			booming = false;
		}

		counter++;
		if (counter % fps == 0) {
			currentFrame++;
		}

	}

	public void setCrit(boolean crit) {
		this.crit = crit;
	}

	public boolean isBooming() {
		return booming;
	}

	public void setX(int x) {
		this.x = x;
		critx = x;
	}

	public void setY(int y) {
		this.y = y;
		crity = y + 50;
	}

	public void resetCounter() {
		counter = 0;
	}

	public void resetFrames() {
		currentFrame = 0;
	}

	public void setToBooming() {
		booming = true;
	}
}
