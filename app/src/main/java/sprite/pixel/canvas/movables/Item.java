package sprite.pixel.canvas.movables;

import android.graphics.Bitmap;

public class Item {

	public int largex, largey;
	public int x, y, itemSpeed;
	public boolean exists;
	public int movementType;
	public int center;
	public boolean moveRight;
	public Bitmap piece;

	public Item(){

	}

	public Item(int speed) {
		y = -100;
		exists = false;
		itemSpeed = speed;
		movementType = (int) (Math.random() * 2);
		if (movementType == 1)
			moveRight = true;
	}

	public Item(int x, int y) {
		this.x = x;
		this.y = y;
		exists = false;
		itemSpeed = 1;
		movementType = (int) (Math.random() * 2);
		if (movementType == 2)
			moveRight = true;
	}

	public void moveItem(int incSpeed) {
		// speed helper is an offset to gradually increase or decrease speed

		switch (movementType) {
		case 0:
			center = x + 30;
			y += itemSpeed + incSpeed;
			break;
		case 1:
			if (moveRight) {
				x += 2;
				center = x + 30;
				y += itemSpeed + incSpeed;
			} else {
				x -= 2;
				center = x + 30;
				y += itemSpeed + incSpeed;
			}
			break;
		}
	}

	public void changeDirection() {
		//if var then !var & if !var then var is the same as
		//var = !var
		if (moveRight)
			moveRight = false;
		else
			moveRight = true;
	}
}
