package sprite.pixel.canvas.movables;

public class LargeItem {

	public Item[] pieces;

	public LargeItem(int speed, int numPieces) {
		pieces = new Item[numPieces];
		for (int i = 0; i < numPieces; i++) {
			pieces[i] = new Item(speed);
			pieces[i].movementType = 0;
		}
	}
}
