package sprite.pixel.canvas.movables;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Effect {

	public int jx1, jy1, jx2, jy2;
	public Paint shieldColor;
	public boolean exists;
	public int shieldCount;

	public Effect() {
		exists = false;
		shieldColor = new Paint();
		shieldColor.setARGB(125, 0, 255, 0);
		shieldCount = 0;
	}

	public void start() {
		exists = true;
		shieldCount = 0;
	}

	public void update(Canvas canvas, int jx1, int jx2, int jy1, int jy2) {
		canvas.drawOval(new RectF(jx1 - shieldCount, jy1 - shieldCount, jx2
				+ shieldCount, jy2 + shieldCount), shieldColor);
		shieldCount+=3;
		if (shieldCount >= 50) {
			exists = false;
		}
	}
}
